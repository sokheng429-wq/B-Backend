package com.bgroceries.backend.entity.Purchase;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", length = 60, nullable = false, unique = true)
    private String code;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "require_date")
    private LocalDate requireDate;

    @Column(name = "purchase_person", length = 100)
    private String purchasePerson;

    @Column(name = "supplier", length = 150)
    private String supplier;

    @Column(name = "supplier_id")
    private Long supplierId;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "grand_total")
    @Builder.Default
    private Double grandTotal = 0.0;

    @Column(name = "balance")
    @Builder.Default
    private Double balance = 0.0;

    @Column(name = "reference", length = 255)
    private String reference;

    @Column(name = "voided_date")
    private LocalDate voidedDate;

    @Column(name = "so_code", length = 60)
    private String soCode;

    @Column(name = "status", length = 50)
    @Builder.Default
    private String status = "OPEN"; // OPEN, PARTIAL, COMPLETED, CLOSED, VOIDED

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "outlet", length = 150)
    private String outlet;

    @Column(name = "payment_term", length = 100)
    private String paymentTerm;

    @Column(name = "shipment_method", length = 100)
    private String shipmentMethod;

    @Column(name = "template_name", length = 100)
    private String templateName;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "sub_amount")
    @Builder.Default
    private Double subAmount = 0.0;

    @Column(name = "discount_percent")
    @Builder.Default
    private Double discountPercent = 0.0;

    @Column(name = "discount_amount")
    @Builder.Default
    private Double discountAmount = 0.0;

    @Column(name = "tax_amount")
    @Builder.Default
    private Double taxAmount = 0.0;

    @Column(name = "billing_address", length = 255)
    private String billingAddress;

    @Column(name = "shipping_address", length = 255)
    private String shippingAddress;

    @Column(name = "carrier", length = 100)
    private String carrier;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PurchaseOrderItem> items = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null || this.status.isBlank()) {
            this.status = "OPEN";
        }
        if (this.grandTotal == null) {
            this.grandTotal = 0.0;
        }
        if (this.balance == null) {
            this.balance = this.grandTotal;
        }
        if (this.date == null) {
            this.date = LocalDate.now();
        }
        if (this.requireDate == null) {
            this.requireDate = LocalDate.now().plusDays(7);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addItem(PurchaseOrderItem item) {
        items.add(item);
        item.setPurchaseOrder(this);
    }

    public void removeItem(PurchaseOrderItem item) {
        items.remove(item);
        item.setPurchaseOrder(null);
    }
}
