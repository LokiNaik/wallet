package com.wallet.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    private Long userId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Version
    private Long version;

    private String description;

    protected Wallet(){}

    public Wallet(Long userId) {
        this.userId = userId;
        this.balance = BigDecimal.ZERO;
    }

    public Long getUserId() {
        return userId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void credit(BigDecimal amount){
        this.balance = this.balance.add(amount);
    }

    public void debit(BigDecimal amount){
        this.balance = this.balance.subtract(amount);
    }
}
