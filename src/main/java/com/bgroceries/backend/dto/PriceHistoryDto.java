package com.bgroceries.backend.dto;

import com.bgroceries.backend.entity.PriceHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * API shape for {@code PriceHistory} — flat DTO with no nested objects.
 * {@code productId} is the FK reference.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceHistoryDto {

    // response-only
    public Long id;
    public LocalDateTime changedAt;

    // FK reference
    public Long productId;

    // price change data
    public BigDecimal oldPrice;
    public BigDecimal newPrice;
    public PriceHistory.ChangeType changeType;
    public BigDecimal markupPercent;
    public String reason;
    public String changedBy;

    // snapshot
    public String productName;
}
