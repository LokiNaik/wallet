package com.wallet.entity;

public enum TransactionType {
    CREDIT,       // Money added to wallet (from system)
    DEBIT,        // Money withdrawn from wallet (to system)
    TRANSFER_SENT, // Money sent to another user
    TRANSFER_RECEIVED // Money received from another user
}
