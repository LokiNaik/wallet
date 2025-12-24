package com.wallet.service;

import com.wallet.dto.*;
import com.wallet.dto.TransactionStatus;
import com.wallet.entity.TransactionType;
import com.wallet.entity.Wallet;
import com.wallet.entity.WalletTransaction;
import com.wallet.exception.InsufficientBalanceException;
import com.wallet.repository.WalletRepository;
import com.wallet.repository.WalletTransactionRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WalletService {

    private static final Logger log = LoggerFactory.getLogger(WalletService.class);
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    @Autowired
    public WalletService(WalletRepository walletRepository, WalletTransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * To find the wallet by User ID.
     */
    private Wallet findWalletById(Long id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wallet not found for the id : " + id));
    }

    /**
     * Create transaction record with PENDING status
     */
    private WalletTransaction createPendingTransaction(
            Long sender,
            Long receiver,
            BigDecimal amount,
            TransactionType type,
            String description,
            Long ownerUserId) { // Add ownerUserId parameter

        WalletTransaction transaction = new WalletTransaction();
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setDescription(description);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setReferenceId("TXN-" + UUID.randomUUID().toString().substring(0, 8));
        transaction.setTransactionTime(LocalDateTime.now());
        transaction.setOwnerUserId(ownerUserId); // Set the owner

        return transactionRepository.save(transaction);
    }

    /**
     * Update transaction status and balance
     */
    private void updateTransactionStatus(WalletTransaction transaction, TransactionStatus status,
                                         BigDecimal balanceAfterTransaction, String failureReason) {
        if (status == TransactionStatus.SUCCESS) {
            transaction.markAsSuccess(balanceAfterTransaction);
        } else if (status == TransactionStatus.FAILED) {
            transaction.markAsFailed(failureReason);
        } else {
            transaction.setStatus(status);
            if (balanceAfterTransaction != null) {
                transaction.setBalanceAfterTransaction(balanceAfterTransaction);
            }
        }
        transactionRepository.save(transaction);
    }

    @Transactional
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public TransactionResponse addMoney(CreditDebitRequest request, Long userId) {
        log.info("Initiating credit of {} for user {}", request.getAmount(), userId);

        // 1. Create pending transaction (owner is the receiver)
        WalletTransaction transaction = createPendingTransaction(
                null,
                userId,
                request.getAmount(),
                TransactionType.CREDIT,
                request.getDescription(),
                userId
        );

        try {
            // 2. Mark as processing
            updateTransactionStatus(transaction, TransactionStatus.PROCESSING, null, null);

            // 3. Process the credit
            Wallet wallet = findWalletById(userId);
            BigDecimal oldBalance = wallet.getBalance();

            wallet.credit(request.getAmount());
            wallet.setDescription(request.getDescription());
            walletRepository.save(wallet);

            BigDecimal newBalance = wallet.getBalance();

            // 4. Mark as successful
            updateTransactionStatus(transaction, TransactionStatus.SUCCESS, newBalance, null);

            log.info("Credit completed for user {}. Old balance: {}, New balance: {}",
                    userId, oldBalance, newBalance);

            return buildTransactionResponse(transaction);

        } catch (Exception e) {
            // 5. Mark as failed on any error
            updateTransactionStatus(transaction, TransactionStatus.FAILED, null, e.getMessage());
            log.error("Credit failed for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Credit failed: " + e.getMessage());
        }
    }

    @Transactional
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public TransactionResponse debit(Long userId, WithdrawalRequest request) {
        log.info("Initiating debit of {} for user {}", request.getAmount(), userId);

        // 1. Create pending transaction (owner is the sender)
        WalletTransaction transaction = createPendingTransaction(
                userId,
                null,
                request.getAmount(),
                TransactionType.DEBIT,
                request.getDescription(),
                userId
        );

        try {
            // 2. Mark as processing
            updateTransactionStatus(transaction, TransactionStatus.PROCESSING, null, null);

            // 3. Process the debit
            Wallet wallet = findWalletById(userId);
            BigDecimal oldBalance = wallet.getBalance();

            if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
                throw new InsufficientBalanceException("Insufficient balance");
            }

            wallet.debit(request.getAmount());
            wallet.setDescription("Withdrawal - " + request.getDescription());
            walletRepository.save(wallet);

            BigDecimal newBalance = wallet.getBalance();

            // 4. Mark as successful
            updateTransactionStatus(transaction, TransactionStatus.SUCCESS, newBalance, null);

            log.info("Debit completed for user {}. Old balance: {}, New balance: {}",
                    userId, oldBalance, newBalance);

            return buildTransactionResponse(transaction);

        } catch (InsufficientBalanceException e) {
            // 5. Mark as failed for insufficient balance
            updateTransactionStatus(transaction, TransactionStatus.DECLINED, null, e.getMessage());
            log.error("Debit declined for user {}: {}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            // 6. Mark as failed for other errors
            updateTransactionStatus(transaction, TransactionStatus.FAILED, null, e.getMessage());
            log.error("Debit failed for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Debit failed: " + e.getMessage());
        }
    }

    @Transactional
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public void transfer(TransferRequest request) {
        Long fromId = request.getSender();
        Long toId = request.getReceiver();
        BigDecimal amount = request.getTransferAmount();

        log.info("Initiating transfer of {} from {} to {}", amount, fromId, toId);

        // 1. Create pending transaction for SENDER (owner is sender)
        WalletTransaction senderTransaction = createPendingTransaction(
                fromId,
                toId,
                amount,
                TransactionType.TRANSFER_SENT,
                request.getDescription() != null ? request.getDescription() : "Transfer to user " + toId,
                fromId
        );

        // 2. Create pending transaction for RECEIVER (owner is receiver)
        WalletTransaction receiverTransaction = createPendingTransaction(
                fromId,
                toId,
                amount,
                TransactionType.TRANSFER_RECEIVED,
                request.getDescription() != null ? request.getDescription() : "Transfer from user " + fromId,
                toId
        );

        try {
            // 3. Mark both as processing
            updateTransactionStatus(senderTransaction, TransactionStatus.PROCESSING, null, null);
            updateTransactionStatus(receiverTransaction, TransactionStatus.PROCESSING, null, null);

            // 4. Process the transfer
            Wallet sender = findWalletById(fromId);
            Wallet receiver = findWalletById(toId);

            if (sender.getBalance().compareTo(amount) < 0) {
                throw new InsufficientBalanceException("Insufficient balance");
            }

            BigDecimal senderOldBalance = sender.getBalance();
            BigDecimal receiverOldBalance = receiver.getBalance();

            sender.debit(amount);
            sender.setDescription(request.getDescription());
            receiver.credit(amount);

            walletRepository.save(sender);
            walletRepository.save(receiver);

            BigDecimal senderNewBalance = sender.getBalance();
            BigDecimal receiverNewBalance = receiver.getBalance();

            // 5. Mark both as successful
            updateTransactionStatus(senderTransaction, TransactionStatus.SUCCESS, senderNewBalance, null);
            updateTransactionStatus(receiverTransaction, TransactionStatus.SUCCESS, receiverNewBalance, null);

            log.info("Transfer completed. Sender: {} -> {}, Receiver: {} -> {}",
                    senderOldBalance, senderNewBalance, receiverOldBalance, receiverNewBalance);

        } catch (InsufficientBalanceException e) {
            // 6. Mark both as declined
            updateTransactionStatus(senderTransaction, TransactionStatus.DECLINED, null, e.getMessage());
            updateTransactionStatus(receiverTransaction, TransactionStatus.DECLINED, null, e.getMessage());
            log.error("Transfer declined from {} to {}: {}", fromId, toId, e.getMessage());
            throw e;
        } catch (Exception e) {
            // 7. Mark both as failed
            updateTransactionStatus(senderTransaction, TransactionStatus.FAILED, null, e.getMessage());
            updateTransactionStatus(receiverTransaction, TransactionStatus.FAILED, null, e.getMessage());
            log.error("Transfer failed from {} to {}: {}", fromId, toId, e.getMessage());
            throw new RuntimeException("Transfer failed: " + e.getMessage());
        }
    }

    /**
     * Cancel a pending transaction
     */
    @Transactional
    public void cancelTransaction(Long transactionId, Long userId) {
        WalletTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // Validate ownership using ownerUserId
        if (!transaction.getOwnerUserId().equals(userId)) {
            throw new RuntimeException("User not authorized to cancel this transaction");
        }

        // Only pending transactions can be cancelled
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new RuntimeException("Only pending transactions can be cancelled");
        }

        transaction.markAsCancelled();
        transactionRepository.save(transaction);

        log.info("Transaction {} cancelled by user {}", transactionId, userId);
    }

    @Transactional
    public BigDecimal checkBalance(Long id) {
        log.info("Balance check for {}", id);
        return walletRepository.findById(id)
                .map(Wallet::getBalance)
                .orElseThrow(() -> new RuntimeException("Wallet not found for id: " + id));
    }

    @Transactional
    public List<WalletTransaction> checkTransactions(Long id) {
        log.info("Transactions for id {}", id);
        // Query by ownerUserId instead of sender/receiver
        return transactionRepository.findByOwnerUserIdOrderByTransactionTimeDesc(id);
    }

    @Transactional
    public List<WalletTransaction> getTransactionsByStatus(Long userId, TransactionStatus status) {
        log.info("Fetching {} transactions for user {}", status, userId);
        return transactionRepository.findByOwnerUserIdAndStatusOrderByTransactionTimeDesc(userId, status);
    }

    /**
     * Build response DTO from transaction entity
     */
    public TransactionResponse buildTransactionResponse(WalletTransaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setTransactionId(transaction.getId());
        response.setReferenceId(transaction.getReferenceId());
        response.setAmount(transaction.getAmount());
        response.setTransactionType(transaction.getType().toString());
        response.setTransactionStatus(transaction.getStatus().toString());
        response.setTransactionDate(transaction.getTransactionTime());
        response.setCompletedDate(transaction.getCompletedTime());
        response.setDescription(transaction.getDescription());
        response.setFailureReason(transaction.getFailureReason());
        response.setBalanceAfterTransaction(transaction.getBalanceAfterTransaction());
        response.setUserId(transaction.getOwnerUserId());

        // Set related user based on transaction type
        if (transaction.getType() == TransactionType.CREDIT) {
            response.setRelatedUserId(transaction.getSender());
        } else if (transaction.getType() == TransactionType.DEBIT) {
            response.setRelatedUserId(transaction.getReceiver());
        } else if (transaction.getType() == TransactionType.TRANSFER_SENT) {
            response.setRelatedUserId(transaction.getReceiver());
        } else if (transaction.getType() == TransactionType.TRANSFER_RECEIVED) {
            response.setRelatedUserId(transaction.getSender());
        }

        return response;
    }
}