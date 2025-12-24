package com.wallet.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionResponse {
    private Long transactionId;
    private String referenceId;
    private BigDecimal amount;
    private String transactionType;
    private String transactionStatus;  // PENDING, SUCCESS, FAILED, etc.
    private LocalDateTime transactionDate;
    private LocalDateTime completedDate;
    private String description;
    private String failureReason;
    private BigDecimal balanceAfterTransaction;
    private Long userId;
    private Long relatedUserId;
}

