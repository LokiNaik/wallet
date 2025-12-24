package com.wallet.dto;

public enum TransactionStatus {
    PENDING,      // Transaction created but not processed
    PROCESSING,   // Transaction being processed
    SUCCESS,      // Transaction completed successfully
    FAILED,       // Transaction failed (insufficient balance, validation error, etc.)
    CANCELLED,    // Transaction cancelled by user/system
    REVERSED,     // Transaction reversed (for rollbacks/refunds)
    DECLINED
}
