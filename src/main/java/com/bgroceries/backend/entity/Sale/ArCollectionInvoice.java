package com.bgroceries.backend.entity.Sale;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ar_collection_invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArCollectionInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ar_collection_id", nullable = false)
    @JsonBackReference
    private ArCollection arCollection;

    @Column(name = "inv_code", length = 60)
    private String invCode;

    @Column(name = "inv_date")
    private LocalDateTime invDate;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "currency", length = 20)
    @Builder.Default
    private String currency = "USD";

    @Column(name = "rate", precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal rate = new BigDecimal("4100.00");

    @Column(name = "amount", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "balance", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "discount", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "pay_amount", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal payAmount = BigDecimal.ZERO;

    @Column(name = "pay_currency", length = 20)
    @Builder.Default
    private String payCurrency = "USD";
}
