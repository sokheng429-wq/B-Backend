package com.bgroceries.backend.entity.Cash;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cash_operations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", length = 60, nullable = false, unique = true)
    private String code;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "type", length = 20, nullable = false) // CASH_IN, CASH_OUT
    private String type;

    @Column(name = "party_type", length = 30) // CUSTOMER, SUPPLIER, OTHER
    private String partyType;

    @Column(name = "party_name", length = 150)
    private String partyName;

    @Column(name = "amount", nullable = false)
    @Builder.Default
    private Double amount = 0.0;

    @Column(name = "outlet", length = 100)
    private String outlet;

    @Column(name = "status", length = 30) // NON_VOIDED, VOIDED
    @Builder.Default
    private String status = "NON_VOIDED";

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "reference_no", length = 80)
    private String referenceNo;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (transactionDate == null) transactionDate = LocalDateTime.now();
        if (status == null) status = "NON_VOIDED";
        if (amount == null) amount = 0.0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
