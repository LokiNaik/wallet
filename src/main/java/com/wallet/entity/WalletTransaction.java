package com.wallet.entity;

import com.wallet.dto.TransactionStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transactions")
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sender;
    private Long receiver;

    @Column(name = "owner_user_id")
    private Long ownerUserId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    private String description;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "balance_after_transaction")
    private BigDecimal balanceAfterTransaction;

    @Column(name = "transaction_time")
    private LocalDateTime transactionTime;

    @Column(name = "completed_time")
    private LocalDateTime completedTime;

    @Column(name = "reference_id")
    private String referenceId;

    // Constructors
    public WalletTransaction() {
        this.transactionTime = LocalDateTime.now();
        this.status = TransactionStatus.PENDING;
    }

    // Business methods
    public void markAsSuccess(BigDecimal balanceAfterTransaction) {
        this.status = TransactionStatus.SUCCESS;
        this.balanceAfterTransaction = balanceAfterTransaction;
        this.completedTime = LocalDateTime.now();
    }

    public void markAsFailed(String failureReason) {
        this.status = TransactionStatus.FAILED;
        this.failureReason = failureReason;
        this.completedTime = LocalDateTime.now();
    }

    public void markAsCancelled() {
        this.status = TransactionStatus.CANCELLED;
        this.completedTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSender() { return sender; }
    public void setSender(Long sender) { this.sender = sender; }

    public Long getReceiver() { return receiver; }
    public void setReceiver(Long receiver) { this.receiver = receiver; }

    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public BigDecimal getBalanceAfterTransaction() { return balanceAfterTransaction; }
    public void setBalanceAfterTransaction(BigDecimal balanceAfterTransaction) {
        this.balanceAfterTransaction = balanceAfterTransaction;
    }

    public LocalDateTime getTransactionTime() { return transactionTime; }
    public void setTransactionTime(LocalDateTime transactionTime) {
        this.transactionTime = transactionTime;
    }

    public LocalDateTime getCompletedTime() { return completedTime; }
    public void setCompletedTime(LocalDateTime completedTime) {
        this.completedTime = completedTime;
    }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
}