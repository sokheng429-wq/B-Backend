package com.bgroceries.backend.entity.Sale;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "consignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", length = 60, nullable = false, unique = true)
    private String code;

    @Column(name = "consignment_date", nullable = false)
    private LocalDateTime consignmentDate;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "customer_name", length = 200)
    private String customerName;

    @Column(name = "customer_phone", length = 60)
    private String customerPhone;

    @Column(name = "customer_address", length = 300)
    private String customerAddress;

    @Column(name = "salesperson", length = 100)
    private String salesperson;

    @Column(name = "payment_term", length = 100)
    private String paymentTerm;

    @Column(name = "outlet", length = 100)
    private String outlet;

    @Column(name = "template_name", length = 100)
    private String templateName;

    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private String status = "OPEN"; // OPEN, COMPLETED, VOIDED

    @Column(name = "sub_amount", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal subAmount = BigDecimal.ZERO;

    @Column(name = "discount_percent", precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "grand_total", precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal grandTotal = BigDecimal.ZERO;

    @Column(name = "balance", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "reference", length = 100)
    private String reference;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "billing_name", length = 200)
    private String billingName;

    @Column(name = "billing_phone", length = 60)
    private String billingPhone;

    @Column(name = "billing_email", length = 100)
    private String billingEmail;

    @Column(name = "billing_address", length = 300)
    private String billingAddress;

    @Column(name = "shipping_recipient", length = 200)
    private String shippingRecipient;

    @Column(name = "shipping_phone", length = 60)
    private String shippingPhone;

    @Column(name = "shipping_address", length = 300)
    private String shippingAddress;

    @Column(name = "shipping_courier", length = 100)
    private String shippingCourier;

    @OneToMany(mappedBy = "consignment", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<ConsignmentItem> items = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.consignmentDate == null) {
            this.consignmentDate = LocalDateTime.now();
        }
        if (this.deliveryDate == null) {
            this.deliveryDate = LocalDateTime.now();
        }
        if (this.status == null || this.status.isBlank()) {
            this.status = "OPEN";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addItem(ConsignmentItem item) {
        items.add(item);
        item.setConsignment(this);
    }

    public void removeItem(ConsignmentItem item) {
        items.remove(item);
        item.setConsignment(null);
    }
}
