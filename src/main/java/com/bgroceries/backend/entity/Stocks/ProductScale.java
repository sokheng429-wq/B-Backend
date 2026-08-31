package com.bgroceries.backend.entity.Stocks;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * PLU / weigh-scale configuration for a {@link Product}. Normally one row per
 * product, used by the admin Scales tool to program the in-store weighing
 * scales with PLU codes and tare weights.
 */
@Entity
@Table(name = "product_scale")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductScale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** PLU code programmed into the weighing scale (up to 10 digits). */
    @Column(name = "plu_code", length = 10, unique = true)
    private String pluCode;

    /** EAN-13 barcode with embedded weight (e.g. 2-prefix variable-weight format). */
    @Column(name = "scale_barcode", length = 20)
    private String scaleBarcode;

    /** Unit of measure used by the scale: Kg, g, Piece, etc. */
    @Column(name = "uom", length = 20)
    private String uom;

    /** Tare weight of the tray or packaging in kilograms (subtracted by the scale). */
    @Column(name = "tare_weight", precision = 8, scale = 3)
    private BigDecimal tareWeight;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

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
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
