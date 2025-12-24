package com.wallet.controller;

import com.wallet.dto.*;
import com.wallet.dto.TransactionStatus;
import com.wallet.entity.WalletTransaction;
import com.wallet.service.WalletService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {
    private static final Logger log = LoggerFactory.getLogger(WalletController.class);
    private final WalletService walletService;

    @Autowired
    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    /**
     * Add amount to wallet.
     *
     * @param userId  User Id
     * @param request Request body
     * @return ResponseEntity.
     */
    @PostMapping("/{userId}/add")
    public ResponseEntity<ApiResponse<TransactionResponse>> credit(
            @PathVariable Long userId,
            @Valid @RequestBody CreditDebitRequest request) {
        TransactionResponse response = walletService.addMoney(request, userId);
        return ResponseEntity.ok(ApiResponse.success("Transaction initiated", response));
    }

    /**
     * Withdrawal API to withdraw amount.
     *
     * @param userId  User Id
     * @param request Request
     * @return Return the response with balance.
     */
    @PostMapping("/{userId}/withdrawal")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdrawal(
            @PathVariable Long userId,
            @Valid @RequestBody WithdrawalRequest request) {
        TransactionResponse response = walletService.debit(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Withdrawal initiated", response));
    }

    /**
     * Transfer API.
     * To transfer the amount from One Wallet to another Wallet using User ID
     *
     * @param transferRequest Request info for transfer
     * @return Response with balance
     */
    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(
            @Valid @RequestBody TransferRequest transferRequest) {
        walletService.transfer(transferRequest);
        return ResponseEntity.ok(ApiResponse.success("Transfer initiated successfully"));
    }

    /**
     * Cancel API.
     * To initiate a Cancel request for the txn which are PENDING/PROCESSING.
     *
     * @param userId        User Id.
     * @param transactionId TransactionId that need to be cancelled.
     * @return Response.
     */
    @PostMapping("/{userId}/transactions/{transactionId}/cancel")
    public ResponseEntity<?> cancelTransaction(
            @PathVariable Long userId,
            @PathVariable Long transactionId) {
        walletService.cancelTransaction(transactionId, userId);
        return ResponseEntity.ok(ApiResponse.success("Transaction cancelled successfully"));
    }


    /**
     * Check Balance API.
     * To check the balance in Wallet using the UserID.
     *
     * @param userId UserId.
     * @return Response with Balance.
     */
    @GetMapping("/{userId}/balance")
    public ResponseEntity<ApiResponse<BalanceResponse>> checkBalance(@PathVariable Long userId) {
        BigDecimal balance = walletService.checkBalance(userId);
        BalanceResponse balanceResponse = new BalanceResponse(userId, balance);
        balanceResponse.setMessage("Balance fetched successfully");
        return ResponseEntity.ok(ApiResponse.success(balanceResponse));
    }

    /**
     * Transactions API.
     * To view all the transactions related to the User fetched by userId.
     *
     * @param userId UserID.
     * @return All the Transactions.
     */
    @GetMapping("/{userId}/transactions")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getHistory(@PathVariable Long userId) {
        List<WalletTransaction> walletTransactions = walletService.checkTransactions(userId);
        List<TransactionResponse> transactionResponses = walletTransactions.stream()
                .map(walletService::buildTransactionResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(transactionResponses));
    }

    /**
     * Transactions/Status API.
     * To view all the Transactions using the status related to the User.
     *
     * @param userId UserId.
     * @param status Status to check.
     * @return ALL the Transactions with the Status.
     */
    @GetMapping("/{userId}/transactions/status/{status}")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionsByStatus(
            @PathVariable Long userId,
            @PathVariable String status) {
        TransactionStatus transactionStatus = TransactionStatus.valueOf(status.toUpperCase());
        List<WalletTransaction> walletTransactions = walletService.getTransactionsByStatus(userId, transactionStatus);
        List<TransactionResponse> transactionResponses = walletTransactions.stream()
                .map(walletService::buildTransactionResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(transactionResponses));
    }
}