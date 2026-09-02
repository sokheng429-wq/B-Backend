package com.bgroceries.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleInvoiceStatsDto {
    private long totalInvoices;
    private long paidInvoices;
    private long unpaidInvoices;
    private long partialInvoices;
    private BigDecimal totalAmount;
    private BigDecimal totalPaid;
    private BigDecimal totalBalance;
    private BigDecimal todaySales;
}