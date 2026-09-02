package com.bgroceries.backend.entity.Sale;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "return_invoice_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnInvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_invoice_id", nullable = false)
    @JsonBackReference
    private ReturnInvoice returnInvoice;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_code", length = 60)
    private String productCode;

    @Column(name = "description", length = 300, nullable = false)
    private String description;

    @Column(name = "qty", precision = 12, scale = 3, nullable = false)
    @Builder.Default
    private BigDecimal qty = BigDecimal.ONE;

    @Column(name = "unit_price", precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(name = "discount", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "uom", length = 50)
    private String uom;

    @Column(name = "total_price", precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal totalPrice = BigDecimal.ZERO;
}