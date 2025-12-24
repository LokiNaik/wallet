package com.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class WithdrawalRequest {

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than or equal to 0.01")
    @Digits(integer = 10, fraction = 2, message = "Amount can have up to 10 integer digits and 2 decimal digits")
    private BigDecimal amount;
    private String description;

    public WithdrawalRequest() {
    }

    public WithdrawalRequest(BigDecimal amount) {
        this.amount = amount;
    }

}
