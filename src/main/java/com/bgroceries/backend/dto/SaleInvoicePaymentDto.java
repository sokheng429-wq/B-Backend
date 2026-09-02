package com.bgroceries.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleInvoicePaymentDto {
    private Long id;
    private Long saleInvoiceId;
    private LocalDateTime paymentDate;
    private BigDecimal amountDollar;
    private BigDecimal amountKhmer;
    private String paymentType;
    private String reference;
    private String note;
    private String receivedBy;
    private LocalDateTime createdAt;
}