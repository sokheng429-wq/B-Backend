package com.bgroceries.backend.entity.Sale;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sale_invoice_payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleInvoicePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_invoice_id", nullable = false)
    @JsonBackReference
    private SaleInvoice saleInvoice;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "amount_dollar", precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal amountDollar = BigDecimal.ZERO;

    @Column(name = "amount_khmer", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal amountKhmer = BigDecimal.ZERO;

    @Column(name = "payment_type", length = 50, nullable = false)
    @Builder.Default
    private String paymentType = "CASH"; // CASH, ABA_QR, BANK_TRANSFER, CREDIT, CARD

    @Column(name = "reference", length = 100)
    private String reference;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "received_by", length = 100)
    private String receivedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.paymentDate == null) this.paymentDate = now;
        this.createdAt = now;
    }
}