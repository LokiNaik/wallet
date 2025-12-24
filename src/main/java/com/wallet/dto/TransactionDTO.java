package com.wallet.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionDTO {
    private Long sender;
    private Long receiver;
    private BigDecimal amount;

}
