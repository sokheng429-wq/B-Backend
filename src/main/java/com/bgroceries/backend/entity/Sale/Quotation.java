package com.bgroceries.backend.entity.Sale;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quotation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", length = 60, nullable = false, unique = true)
    private String code;

    @Column(name = "quotation_date", nullable = false)
    private LocalDateTime quotationDate;

    @Column(name = "expired_date")
    private LocalDateTime expiredDate;

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
    private String status = "DRAFT"; // DRAFT, SENT, APPROVED, REJECTED, CONVERTED, EXPIRED

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

    @Column(name = "billing_name", length = 200)
    private String billingName;

    @Column(name = "billing_phone", length = 60)
    private String billingPhone;

    @Column(name = "billing_email", length = 100)
    private String billingEmail;

    @Column(name = "billing_address", length = 300)
    private String billingAddress;

    @Column(name = "billing_city", length = 100)
    private String billingCity;

    @Column(name = "billing_tax_no", length = 60)
    private String billingTaxNo;

    @Column(name = "shipping_name", length = 200)
    private String shippingName;

    @Column(name = "shipping_phone", length = 60)
    private String shippingPhone;

    @Column(name = "shipping_address", length = 300)
    private String shippingAddress;

    @Column(name = "shipping_city", length = 100)
    private String shippingCity;

    @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<QuotationItem> items = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.quotationDate == null) {
            this.quotationDate = LocalDateTime.now();
        }
        if (this.status == null || this.status.isBlank()) {
            this.status = "DRAFT";
        }
        if (this.subAmount == null) this.subAmount = BigDecimal.ZERO;
        if (this.discountPercent == null) this.discountPercent = BigDecimal.ZERO;
        if (this.discountAmount == null) this.discountAmount = BigDecimal.ZERO;
        if (this.taxAmount == null) this.taxAmount = BigDecimal.ZERO;
        if (this.markupAmount == null) this.markupAmount = BigDecimal.ZERO;
        if (this.grandTotal == null) this.grandTotal = BigDecimal.ZERO;
        if (this.balance == null) this.balance = BigDecimal.ZERO;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
