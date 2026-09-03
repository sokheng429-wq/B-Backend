package com.bgroceries.backend.entity.Sale;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "sale_order_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleOrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_order_id", nullable = false)
    @JsonBackReference
    private SaleOrder saleOrder;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_code", length = 60)
    private String productCode;

    @Column(name = "barcode", length = 60)
    private String barcode;

    @Column(name = "description", length = 255, nullable = false)
    private String description;

    @Column(name = "qty", precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal qty = BigDecimal.ONE;

    @Column(name = "price", precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "discount", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "uom", length = 50)
    @Builder.Default
    private String uom = "PCS";

    @Column(name = "total", precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;
}
