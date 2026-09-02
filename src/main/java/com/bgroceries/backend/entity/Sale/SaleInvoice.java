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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sale_invoice")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_code", length = 60, nullable = false, unique = true)
    private String invoiceCode;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "so_code", length = 60)
    private String soCode;

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

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "template_name", length = 100)
    private String templateName;

    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private String status = "UNPAID"; // UNPAID, PARTIAL, PAID, VOID, CREDIT

    @Column(name = "sub_total", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal subTotal = BigDecimal.ZERO;

    @Column(name = "discount_percent", precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "tax_percent", precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal taxPercent = BigDecimal.ZERO;

    @Column(name = "markup_amount", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal markupAmount = BigDecimal.ZERO;

    @Column(name = "grand_total", precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal grandTotal = BigDecimal.ZERO;

    @Column(name = "paid_amount", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "balance", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "exchange_rate", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal exchangeRate = new BigDecimal("4100.00");

    @Column(name = "grand_total_khmer", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal grandTotalKhmer = BigDecimal.ZERO;

    @Column(name = "barcode", length = 100)
    private String barcode;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "payment_type", length = 50)
    private String paymentType;

    // Billing info snapshot
    @Column(name = "billing_name", length = 200)
    private String billingName;

    @Column(name = "billing_phone", length = 60)
    private String billingPhone;

    @Column(name = "billing_email", length = 150)
    private String billingEmail;

    @Column(name = "billing_address", length = 300)
    private String billingAddress;

    @Column(name = "billing_city", length = 100)
    private String billingCity;

    @Column(name = "billing_tax_no", length = 100)
    private String billingTaxNo;

    // Shipping info snapshot
    @Column(name = "shipping_recipient", length = 200)
    private String shippingRecipient;

    @Column(name = "shipping_phone", length = 60)
    private String shippingPhone;

    @Column(name = "shipping_address", length = 300)
    private String shippingAddress;

    @Column(name = "shipping_method", length = 100)
    private String shippingMethod;

    @Column(name = "tracking_no", length = 100)
    private String trackingNo;

    @OneToMany(mappedBy = "saleInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<SaleInvoiceItem> lines = new ArrayList<>();

    @OneToMany(mappedBy = "saleInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<SaleInvoicePayment> payments = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) this.status = "UNPAID";
        if (this.subTotal == null) this.subTotal = BigDecimal.ZERO;
        if (this.discountPercent == null) this.discountPercent = BigDecimal.ZERO;
        if (this.discountAmount == null) this.discountAmount = BigDecimal.ZERO;
        if (this.taxAmount == null) this.taxAmount = BigDecimal.ZERO;
        if (this.taxPercent == null) this.taxPercent = BigDecimal.ZERO;
        if (this.markupAmount == null) this.markupAmount = BigDecimal.ZERO;
        if (this.grandTotal == null) this.grandTotal = BigDecimal.ZERO;
        if (this.paidAmount == null) this.paidAmount = BigDecimal.ZERO;
        if (this.balance == null) this.balance = this.grandTotal.subtract(this.paidAmount);
        if (this.exchangeRate == null) this.exchangeRate = new BigDecimal("4100.00");
        if (this.grandTotalKhmer == null || this.grandTotalKhmer.compareTo(BigDecimal.ZERO) == 0) {
            this.grandTotalKhmer = this.grandTotal.multiply(this.exchangeRate);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.grandTotal != null && this.paidAmount != null) {
            this.balance = this.grandTotal.subtract(this.paidAmount);
        }
    }
}