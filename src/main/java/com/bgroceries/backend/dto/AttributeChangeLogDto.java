package com.bgroceries.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * API shape for {@code AttributeChangeLog} — flat DTO with no nested objects.
 * {@code productId} is the FK reference.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttributeChangeLogDto {

    // response-only
    public Long id;
    public LocalDateTime changedAt;

    // FK reference
    public Long productId;

    // attribute change data
    public String attributeName;
    public String oldValue;
    public String newValue;
    public String reason;
    public String changedBy;

    // snapshot
    public String productName;
}
