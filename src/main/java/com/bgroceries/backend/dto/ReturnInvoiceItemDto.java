package com.bgroceries.backend.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnInvoiceItemDto {
    private Long id;
    private Long productId;
    private String productCode;
    private String description;
    private BigDecimal qty;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private String uom;
    private BigDecimal totalPrice;
}