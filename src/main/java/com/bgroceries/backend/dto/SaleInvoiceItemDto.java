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
public class SaleInvoiceItemDto {
    private Long id;
    private Long saleInvoiceId;
    private Long productId;
    private String productCode;
    private String description;
    private BigDecimal qty;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private String uom;
    private BigDecimal totalPrice;
}