package com.bgroceries.backend.entity.Sale;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_refunds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRefund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", length = 60, nullable = false, unique = true)
    private String code;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "rate", precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal rate = new BigDecimal("4100.00");

    @Column(name = "paid_amount", precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "balance", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "partner", length = 200, nullable = false)
    private String partner;

    @Column(name = "contact", length = 100)
    private String contact;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "user_name", length = 100)
    @Builder.Default
    private String username = "Admin";

    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private String status = "NONE_VOID"; // NONE_VOID, VOIDED

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "payment_type", length = 60)
    @Builder.Default
    private String paymentType = "Cash";

    @Column(name = "authorization_note", length = 500)
    private String authorizationNote;

    @OneToMany(mappedBy = "customerRefund", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<CustomerRefundInvoice> invoices = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.paymentDate == null) {
            this.paymentDate = LocalDateTime.now();
        }
        if (this.status == null || this.status.isBlank()) {
            this.status = "NONE_VOID";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
