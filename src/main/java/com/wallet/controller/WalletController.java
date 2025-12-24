package com.wallet.controller;

import com.wallet.dto.*;
import com.wallet.entity.TransactionType;
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

    @PostMapping("/{userId}/add")
    public ResponseEntity<ApiResponse<Void>> credit(
            @PathVariable Long userId,
            @Valid @RequestBody CreditDebitRequest request) {
        walletService.addMoney(request, userId);
        return ResponseEntity.ok(ApiResponse.success("Amount Credited Successfully!"));
    }

    @PostMapping("/{userId}/withdrawal")
    public ResponseEntity<ApiResponse<Void>> withdrawal(
            @PathVariable Long userId,
            @Valid @RequestBody WithdrawalRequest request) {
        walletService.debit(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Amount withdrawn successfully"));
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<Void>> transfer(
            @Valid @RequestBody TransferRequest transferRequest) {
        walletService.transfer(transferRequest);
        return ResponseEntity.ok(ApiResponse.success("Transfer completed successfully"));
    }

    @GetMapping("/{userId}/balance")
    public ResponseEntity<ApiResponse<BalanceResponse>> checkBalance(@PathVariable Long userId) {
        BigDecimal balance = walletService.checkBalance(userId);
        BalanceResponse balanceResponse = new BalanceResponse(userId, balance);
        balanceResponse.setMessage("Balance fetched successfully");
        return ResponseEntity.ok(ApiResponse.success(balanceResponse));
    }

    @GetMapping("/{userId}/transactions")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getHistory(@PathVariable Long userId) {
        List<WalletTransaction> walletTransactions = walletService.checkTransactions(userId);
        List<TransactionResponse> transactionResponses = walletTransactions.stream()
                .map(transaction -> convertToTransactionResponse(transaction, userId))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(transactionResponses));
    }

    private TransactionResponse convertToTransactionResponse(
            WalletTransaction transaction, Long currentUserId) {

        TransactionResponse response = new TransactionResponse();

        response.setTransactionId(transaction.getId());
        response.setAmount(transaction.getAmount());
        response.setTransactionType(transaction.getType());
        response.setTransactionDate(transaction.getTransactionTime());
        response.setDescription(transaction.getDescription());
        response.setStatus(transaction.getStatus());

        // Perspective handling
        if (transaction.getSender() != null && transaction.getSender().equals(currentUserId)) {
            response.setUserId(transaction.getSender());
            response.setRelatedUserId(transaction.getReceiver());
        } else {
            response.setUserId(transaction.getReceiver());
            response.setRelatedUserId(transaction.getSender());
        }

        // ✅ HISTORICAL BALANCE (CORRECT)
        response.setBalanceAfterTransaction(transaction.getBalanceAfterTransaction());

        return response;
    }

}