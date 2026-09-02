package com.bgroceries.backend.entity.Sale;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "return_invoice")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_code", length = 60, nullable = false, unique = true)
    private String invoiceCode; // e.g. RET-260902-0001

    @Column(name = "apply_to_invoice", length = 60)
    private String applyToInvoice; // Reference to original sale invoice code

    @Column(name = "return_date", nullable = false)
    private LocalDate returnDate;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "customer_name", length = 200)
    private String customerName;

    @Column(name = "customer_phone", length = 60)
    private String customerPhone;

    @Column(name = "customer_address", length = 300)
    private String customerAddress;

    @Column(name = "grand_total", precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal grandTotal = BigDecimal.ZERO;

    @Column(name = "balance", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "tax_code", length = 50)
    private String taxCode;

    @Column(name = "payment_term", length = 100)
    private String paymentTerm;

    @Column(name = "salesperson", length = 100)
    private String salesperson;

    @Column(name = "markup", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal markup = BigDecimal.ZERO;

    @Column(name = "outlet", length = 100)
    private String outlet;

    @Column(name = "user_name", length = 100)
    private String username;

    @Column(name = "so_code", length = 60)
    private String soCode;

    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private String status = "RETURNED"; // RETURNED, REFUNDED, PENDING, REJECTED

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @OneToMany(mappedBy = "returnInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<ReturnInvoiceItem> lines = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.returnDate == null) this.returnDate = LocalDate.now();
        if (this.status == null) this.status = "RETURNED";
        if (this.grandTotal == null) this.grandTotal = BigDecimal.ZERO;
        if (this.balance == null) this.balance = BigDecimal.ZERO;
        if (this.markup == null) this.markup = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}