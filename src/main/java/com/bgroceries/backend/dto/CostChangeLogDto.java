package com.bgroceries.backend.dto;

import com.bgroceries.backend.entity.CostChangeLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * API shape for {@code CostChangeLog} — flat DTO with no nested objects.
 * {@code productId} is the FK reference.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostChangeLogDto {

    // response-only
    public Long id;
    public LocalDateTime changedAt;

    // FK reference
    public Long productId;

    // cost change data
    public BigDecimal oldCost;
    public BigDecimal newCost;
    public CostChangeLog.AdjustmentType adjustmentType;
    public BigDecimal adjustmentValue;
    public String reason;
    public String changedBy;

    // snapshot
    public String productName;
}
