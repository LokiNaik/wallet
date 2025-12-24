package com.wallet.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class BalanceResponse {

    private Long userId;
    private BigDecimal balance;
    private String currency; // e.g., "USD", "EUR", etc.
    private LocalDateTime timestamp;
    private String message;

    public BalanceResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public BalanceResponse(Long userId, BigDecimal balance) {
        this.userId = userId;
        this.balance = balance;
        this.currency = "INR"; // Default or configurable
        this.timestamp = LocalDateTime.now();
    }
}
