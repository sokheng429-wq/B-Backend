package com.bgroceries.backend.entity.Sale;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_refund_invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRefundInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_refund_id", nullable = false)
    @JsonBackReference
    private CustomerRefund customerRefund;

    @Column(name = "code", length = 60)
    private String code;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "type", length = 60)
    @Builder.Default
    private String type = "Return Invoice";

    @Column(name = "amount", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "balance", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "pay_amount", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal payAmount = BigDecimal.ZERO;

    @Column(name = "pay_currency", length = 20)
    @Builder.Default
    private String payCurrency = "USD";
}
