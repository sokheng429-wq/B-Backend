package com.bgroceries.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * API shape for {@code ProductSupplierLink} — flat DTO with no nested objects.
 * {@code productId} and {@code supplierId} are the FK references.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSupplierLinkDto {

    // response-only
    public Long id;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    // FK references
    public Long productId;
    public String productName;
    public Long supplierId;
    public String supplierName;

    // link metadata
    public String vendorPartNumber;
    public BigDecimal contractedCost;
    public Integer leadTimeDays;
    public Boolean preferred;
    public Boolean active;
}
