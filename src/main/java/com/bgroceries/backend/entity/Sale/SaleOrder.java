package com.bgroceries.backend.entity.Sale;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sale_order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", length = 60, nullable = false, unique = true)
    private String code;

    @Column(name = "quote_code", length = 60)
    private String quoteCode;

    @Column(name = "po_code", length = 60)
    private String poCode;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "customer_name", length = 200)
    private String customerName;

    @Column(name = "customer_phone", length = 60)
    private String customerPhone;

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
    private String status = "CONFIRMED"; // DRAFT, CONFIRMED, PROCESSING, BILLED, CANCELLED

    @Column(name = "credit_limit", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @Column(name = "available_credit", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal availableCredit = BigDecimal.ZERO;

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

    @Column(name = "markup_amount", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal markupAmount = BigDecimal.ZERO;

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

    @Column(name = "related_purchase_order", length = 100)
    private String relatedPurchaseOrder;

    @OneToMany(mappedBy = "saleOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<SaleOrderItem> items = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.orderDate == null) this.orderDate = LocalDateTime.now();
        if (this.status == null) this.status = "CONFIRMED";
        if (this.subAmount == null) this.subAmount = BigDecimal.ZERO;
        if (this.grandTotal == null) this.grandTotal = BigDecimal.ZERO;
        if (this.balance == null) this.balance = this.grandTotal;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
