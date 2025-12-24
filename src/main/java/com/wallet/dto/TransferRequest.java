package com.wallet.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@Getter
@Setter
public class TransferRequest {

    private Long sender;
    private Long receiver;
    private BigDecimal transferAmount;
    private String description;

    public TransferRequest(Long receiver, Long sender, BigDecimal transferAmount) {
        this.receiver = receiver;
        this.sender = sender;
        this.transferAmount = transferAmount;
    }
}
