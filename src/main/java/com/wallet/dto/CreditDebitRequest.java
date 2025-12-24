package com.wallet.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

// Getters and Setters
@Setter
@Getter
public class CreditDebitRequest {

    @NotNull(message = "Amount is required")
    @Min(value = 0, message = "Amount must be positive")
    @Digits(integer = 10, fraction = 2, message = "Amount can have up to 10 integer digits and 2 decimal digits")
    private BigDecimal amount;

    private String description;

    public CreditDebitRequest() {
    }

    public CreditDebitRequest(BigDecimal amount) {
        this.amount = amount;
    }

    public CreditDebitRequest(BigDecimal amount, String description) {
        this.amount = amount;
        this.description = description;
    }

}
