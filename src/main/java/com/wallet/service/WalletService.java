package com.wallet.service;

import com.wallet.dto.CreditDebitRequest;
import com.wallet.dto.TransferRequest;
import com.wallet.dto.WithdrawalRequest;
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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
     * Enhanced transaction recording with balance after transaction.
     */
    private WalletTransaction recordTransaction(
            Long ownerUserId,
            Long sender,
            Long receiver,
            BigDecimal amount,
            TransactionType type,
            String description,
            BigDecimal balanceAfterTransaction) {

        WalletTransaction transaction = new WalletTransaction();
        transaction.setOwnerUserId(ownerUserId);
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setDescription(description);
        transaction.setBalanceAfterTransaction(balanceAfterTransaction);
        transaction.setStatus("SUCCESS");
        transaction.setTransactionTime(LocalDateTime.now());

        return transactionRepository.save(transaction);
    }

    @Transactional
    public void addMoney(CreditDebitRequest request, Long id) {
        BigDecimal amount = request.getAmount();
        log.info("Credit amount {} for id {}", amount, id);

        Wallet wallet = findWalletById(id);
        BigDecimal oldBalance = wallet.getBalance();

        wallet.credit(amount);
        wallet.setDescription(request.getDescription());
        walletRepository.save(wallet);

        BigDecimal newBalance = wallet.getBalance();

        // Record transaction with correct balance
        recordTransaction(
                id, // owner
                null, // sender (system)
                id,   // receiver
                amount,
                TransactionType.CREDIT,
                request.getDescription(),
                newBalance
        );

        log.info("Credit completed. Old balance: {}, New balance: {}", oldBalance, newBalance);
    }

    @Transactional
    public void debit(Long id, WithdrawalRequest request) {
        BigDecimal amount = request.getAmount();
        log.info("Debit amount {} for id {}", amount, id);

        Wallet wallet = findWalletById(id);
        BigDecimal oldBalance = wallet.getBalance();

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        wallet.debit(amount);
        wallet.setDescription("Withdrawal - "+request.getDescription());
        walletRepository.save(wallet);

        BigDecimal newBalance = wallet.getBalance();

        // Record transaction with correct balance
        recordTransaction(
                id, // owner
                id,   // sender
                null, // receiver (system)
                amount,
                TransactionType.DEBIT,
                request.getDescription(),
                newBalance
        );

        log.info("Debit completed. Old balance: {}, New balance: {}", oldBalance, newBalance);
    }

    @Transactional
    public void transfer(TransferRequest request) {

        Long fromId = request.getSender();
        Long toId = request.getReceiver();
        BigDecimal amount = request.getTransferAmount();

        log.info("Transferring amount {} from {} to {}", amount, fromId, toId);

        Wallet sender = findWalletById(fromId);
        Wallet receiver = findWalletById(toId);

        if (sender.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        BigDecimal senderOldBalance = sender.getBalance();
        BigDecimal receiverOldBalance = receiver.getBalance();

        // Perform transfer
        sender.debit(amount);
        receiver.credit(amount);

        walletRepository.save(sender);
        walletRepository.save(receiver);

        BigDecimal senderNewBalance = sender.getBalance();
        BigDecimal receiverNewBalance = receiver.getBalance();

        /* ---------------- SENDER TRANSACTION ---------------- */
        recordTransaction(
                fromId,
                fromId,
                toId,
                amount,
                TransactionType.TRANSFER,
                request.getDescription() != null
                        ? request.getDescription()
                        : "Transfer to user " + toId,
                senderNewBalance
        );

        /* ---------------- RECEIVER TRANSACTION ---------------- */
        recordTransaction(
                toId,
                toId,
                fromId,
                amount,
                TransactionType.TRANSFER,
                request.getDescription() != null
                        ? request.getDescription()
                        : "Transfer from user " + fromId,
                receiverNewBalance
        );

        log.info("Transfer completed. Sender: {} -> {}, Receiver: {} -> {}",
                senderOldBalance, senderNewBalance,
                receiverOldBalance, receiverNewBalance);
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
        return transactionRepository.findByOwnerUserId(id, id)
                .stream()
                .sorted((t1, t2) -> t2.getTransactionTime().compareTo(t1.getTransactionTime()))
                .collect(Collectors.toList());
    }
}