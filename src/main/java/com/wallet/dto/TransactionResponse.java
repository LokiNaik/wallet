package com.wallet.dto;

import com.wallet.entity.TransactionType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionResponse {

    private Long transactionId;
    private Long userId;
    private TransactionType transactionType; // CREDIT, DEBIT, TRANSFER
    private BigDecimal amount;
    private BigDecimal balanceAfterTransaction;
    private String description;
    private String status; // SUCCESS, FAILED, PENDING
    private LocalDateTime transactionDate;
    private Long relatedUserId;

    public TransactionResponse() {
    }

    @Override
    public String toString() {
        return "TransactionResponse{" +
                "transactionId=" + transactionId +
                ", userId=" + userId +
                ", transactionType='" + transactionType + '\'' +
                ", amount=" + amount +
                ", balanceAfterTransaction=" + balanceAfterTransaction +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", transactionDate=" + transactionDate +
                ", relatedUserId=" + relatedUserId +
                '}';
    }
}
