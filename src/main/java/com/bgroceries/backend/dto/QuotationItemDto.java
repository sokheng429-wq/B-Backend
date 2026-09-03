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
public class QuotationItemDto {
    private Long id;
    private Long productId;
    private String productCode;
    private String barcode;
    private String description;
    private BigDecimal qty;
    private BigDecimal price;
    private BigDecimal discount;
    private String uom;
    private BigDecimal total;
    private String note;
}
