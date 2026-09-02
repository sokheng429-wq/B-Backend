package com.bgroceries.backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalePromotionDto {
    private Long id;
    private String code;
    private String description;
    private String secondLanguage;
    private Boolean active;
    private String priceBook;
    private String discountType;
    private String minRequirementType;
    private BigDecimal minRequirementValue;
    private String discountValueScope;
    private Long targetScopeId;
    private String targetScopeName;
    private BigDecimal discountValue;
    private String dateType;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}