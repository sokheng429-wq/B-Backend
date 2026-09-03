package com.bgroceries.backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArCollectionInvoiceDto {
    private Long id;
    private String invCode;
    private LocalDateTime invDate;
    private LocalDateTime dueDate;
    private String currency;
    private BigDecimal rate;
    private BigDecimal amount;
    private BigDecimal balance;
    private BigDecimal discount;
    private BigDecimal payAmount;
    private String payCurrency;
}
