package com.bgroceries.backend.entity.Stocks;

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
 * Audit trail for selling price changes made via the admin Products Prices tool.
 * Every price update — manual edits, bulk markup passes and imports — is
 * recorded here so price history is always queryable per product.
 */
@Entity
@Table(name = "price_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceHistory {

    /** How the price change was triggered. */
    public enum ChangeType { MANUAL, BULK_MARKUP, IMPORT }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "old_price", precision = 12, scale = 2)
    private BigDecimal oldPrice;

    @Column(name = "new_price", precision = 12, scale = 2)
    private BigDecimal newPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", length = 20)
    private ChangeType changeType;

    /** Markup percentage used when changeType is BULK_MARKUP; null otherwise. */
    @Column(name = "markup_percent", precision = 6, scale = 2)
    private BigDecimal markupPercent;

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
