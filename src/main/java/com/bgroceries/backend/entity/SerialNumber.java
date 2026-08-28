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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Tracks individual serialized or batched product units. Each row represents
 * one serial / batch-lot entry linked to the {@link Product} it belongs to and,
 * optionally, the {@link StockLine} that created it on a receive document.
 */
@Entity
@Table(name = "serial_number")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SerialNumber {

    /** Lifecycle status of a single serialized / batched unit. */
    public enum Status { ACTIVE, SOLD, EXPIRED, DAMAGED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** The receive line that created this serial — nullable for manually entered serials. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_line_id")
    private StockLine stockLine;

    @Column(name = "serial_number", length = 100, nullable = false)
    private String serialNumber;

    @Column(name = "batch_lot", length = 80)
    private String batchLot;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status = Status.ACTIVE;

    /** Snapshot of product.code at time of entry — survives product code changes. */
    @Column(name = "product_code", length = 50)
    private String productCode;

    /** Snapshot of product.name at time of entry. */
    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = Status.ACTIVE;
    }
}
