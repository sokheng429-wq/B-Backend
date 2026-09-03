package com.bgroceries.backend.entity.Sale;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_deposits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDeposit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", length = 60, nullable = false, unique = true)
    private String code;

    @Column(name = "deposit_date", nullable = false)
    private LocalDateTime depositDate;

    @Column(name = "amount", precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "customer_name", length = 200, nullable = false)
    private String customerName;

    @Column(name = "contact", length = 100)
    private String contact;

    @Column(name = "payment_type", length = 60)
    @Builder.Default
    private String paymentType = "Cash";

    @Column(name = "reference", length = 100)
    private String reference;

    @Column(name = "username", length = 100)
    @Builder.Default
    private String username = "Admin";

    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private String status = "NONE_VOID"; // NONE_VOID, VOIDED

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.depositDate == null) {
            this.depositDate = LocalDateTime.now();
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
