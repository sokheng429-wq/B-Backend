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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "sale_invoice_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleInvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_invoice_id", nullable = false)
    @JsonBackReference
    private SaleInvoice saleInvoice;

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