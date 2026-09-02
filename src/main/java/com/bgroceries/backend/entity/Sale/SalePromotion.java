package com.bgroceries.backend.entity.Sale;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sale_promotion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalePromotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", length = 60, nullable = false, unique = true)
    private String code;

    @Column(name = "description", length = 300, nullable = false)
    private String description;

    @Column(name = "second_language", length = 300)
    private String secondLanguage;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "price_book", length = 100)
    private String priceBook; // Standard Retail, Wholesale Book, VIP Price Book, Member Tier 1

    @Column(name = "discount_type", length = 50, nullable = false)
    @Builder.Default
    private String discountType = "PERCENTAGE"; // PERCENTAGE, FIXED_AMOUNT, BUY_X_GET_Y

    @Column(name = "min_requirement_type", length = 50)
    @Builder.Default
    private String minRequirementType = "ENTIRE_ORDER"; // ENTIRE_ORDER, MIN_PURCHASE_AMOUNT, MIN_QUANTITY

    @Column(name = "min_requirement_value", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal minRequirementValue = BigDecimal.ZERO;

    @Column(name = "discount_value_scope", length = 50)
    @Builder.Default
    private String discountValueScope = "ENTIRE_ORDER"; // ENTIRE_ORDER, SPECIFIC_PRODUCT_GROUP, SPECIFIC_PRODUCT

    @Column(name = "target_scope_id")
    private Long targetScopeId; // Product ID or Product Group ID if scoped

    @Column(name = "target_scope_name", length = 200)
    private String targetScopeName;

    @Column(name = "discount_value", precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal discountValue = BigDecimal.ZERO; // e.g. 10 for 10%, or 5.00 for .00

    @Column(name = "date_type", length = 50)
    @Builder.Default
    private String dateType = "INTERVAL"; // INTERVAL, RECURRENT

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.active == null) this.active = true;
        if (this.discountType == null) this.discountType = "PERCENTAGE";
        if (this.minRequirementType == null) this.minRequirementType = "ENTIRE_ORDER";
        if (this.discountValueScope == null) this.discountValueScope = "ENTIRE_ORDER";
        if (this.discountValue == null) this.discountValue = BigDecimal.ZERO;
        if (this.dateType == null) this.dateType = "INTERVAL";
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}