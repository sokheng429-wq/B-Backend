package com.bgroceries.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * API shape for {@code ProductScale} — flat DTO with no nested objects.
 * {@code productId} is the FK reference.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductScaleDto {

    // response-only
    public Long id;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    // FK reference
    public Long productId;
    public String productName;

    // scale config
    public String pluCode;
    public String scaleBarcode;
    public String uom;
    public BigDecimal tareWeight;
    public Boolean active;
}
