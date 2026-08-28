package com.bgroceries.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Audit trail for cost adjustments made via the admin Cost Change tool.
 * Every change to a product's standard / average cost is appended here
 * so the full cost history is available for reporting and reversal.
 */
@Entity
@Table(name = "cost_change_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostChangeLog {

    /** Whether the adjustment was a percentage uplift or a fixed delta. */
    public enum AdjustmentType { PERCENTAGE, FIXED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "old_cost", precision = 12, scale = 2)
    private BigDecimal oldCost;

    @Column(name = "new_cost", precision = 12, scale = 2)
    private BigDecimal newCost;

    @Enumerated(EnumType.STRING)
    @Column(name = "adjustment_type", length = 20)
    private AdjustmentType adjustmentType;

    /** The percentage (e.g. 10.5000) or fixed delta amount applied. */
    @Column(name = "adjustment_value", precision = 10, scale = 4)
    private BigDecimal adjustmentValue;

    @Column(name = "reason", length = 200)
    private String reason;

    @Column(name = "changed_by", length = 100)
    private String changedBy;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;

    /** Snapshot of product.name at the time of the change. */
    @Column(name = "product_name", length = 200)
    private String productName;

    @PrePersist
    protected void onCreate() {
        this.changedAt = LocalDateTime.now();
    }
}
