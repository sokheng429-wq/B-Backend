package com.bgroceries.backend.entity.Sale;

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

@Entity
@Table(name = "aging_invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgingInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Code
    @Column(name = "code", length = 60, nullable = false, unique = true)
    private String code;

    // Date
    @Column(name = "invoice_date", nullable = false)
    private LocalDate date;

    // Due Date
    @Column(name = "due_date")
    private LocalDate dueDate;

    // Customer
    @Column(name = "customer", length = 200, nullable = false)
    private String customer;

    // Contact Name
    @Column(name = "contact_name", length = 150)
    private String contactName;

    // Phone
    @Column(name = "phone", length = 60)
    private String phone;

    // Status
    @Column(name = "status", length = 50, nullable = false)
    @Builder.Default
    private String status = "UNPAID";

    // Grand Total
    @Column(name = "grand_total", precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal grandTotal = BigDecimal.ZERO;

    // Balance
    @Column(name = "balance", precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    // Advance filter attributes
    @Column(name = "salesperson", length = 100)
    private String salesperson;

    @Column(name = "customer_group", length = 100)
    private String customerGroup;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.grandTotal == null) this.grandTotal = BigDecimal.ZERO;
        if (this.balance == null) this.balance = BigDecimal.ZERO;
        if (this.status == null) this.status = "UNPAID";
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
