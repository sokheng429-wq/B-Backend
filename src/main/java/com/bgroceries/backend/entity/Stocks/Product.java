package com.bgroceries.backend.entity.Stocks;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A product in the B'Groceries shop catalog, managed from the admin Stocks →
 * Products pages. Master-data references (group, category, brand, supplier,
 * UOM) are stored as plain strings for now — the dedicated management pages
 * can be upgraded to foreign keys later without changing the API shape.
 */
@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Internal product code/SKU, unique when provided. */
    @Column(name = "code", length = 50, unique = true)
    private String code;

    @Column(name = "bar_code", length = 64)
    private String barCode;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /** Second-language (Khmer) display name. */
    @Column(name = "name_kh", length = 200)
    private String nameKh;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "product_group", length = 100)
    private String productGroup;

    @Column(name = "category", length = 100)
    private String category;

    /** Units physically on hand. Decimal so weighed goods (kg, L) work. */
    @Column(name = "on_hand", precision = 12, scale = 3)
    private BigDecimal onHand;

    @Column(name = "uom", length = 30)
    private String uom;

    @Column(name = "base_price", precision = 12, scale = 2)
    private BigDecimal basePrice;

    /** Average (available) cost from supplier invoices. */
    @Column(name = "average_cost", precision = 12, scale = 2)
    private BigDecimal averageCost;

    @Column(name = "standard_cost", precision = 12, scale = 2)
    private BigDecimal standardCost;

    /** Business-facing creation date (defaults to today); distinct from the created_at audit stamp. */
    @Column(name = "create_date")
    private LocalDate createDate;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "supplier", length = 150)
    private String supplier;

    @Column(name = "part_number", length = 80)
    private String partNumber;

    @Column(name = "brand", length = 100)
    private String brand;

    /** Quantity incoming on purchase orders. */
    @Column(name = "on_po", precision = 12, scale = 3)
    private BigDecimal onPo;

    /** Quantity committed on sales orders. */
    @Column(name = "on_so", precision = 12, scale = 3)
    private BigDecimal onSo;

    @Column(name = "available_stock", precision = 12, scale = 3)
    private BigDecimal availableStock;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "serial", length = 100)
    private String serial;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Builder.Default
    @Column(name = "allow_discount", nullable = false)
    private Boolean allowDiscount = true;

    /** Tax rate percent (e.g. 10.00). */
    @Column(name = "tax", precision = 5, scale = 2)
    private BigDecimal tax;

    @Builder.Default
    @Column(name = "out_of_stock", nullable = false)
    private Boolean outOfStock = false;

    @Builder.Default
    @Column(name = "favorite", nullable = false)
    private Boolean favorite = false;

    /** Product photo — a URL, or a compressed base64 data URL picked on the
     * Add-Product page (data URLs run tens of thousands of chars). */
    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.createDate == null) {
            this.createDate = LocalDate.now();
        }
        if (this.active == null) this.active = true;
        if (this.allowDiscount == null) this.allowDiscount = true;
        if (this.outOfStock == null) this.outOfStock = false;
        if (this.favorite == null) this.favorite = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
