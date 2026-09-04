package com.bgroceries.backend.entity.Purchase;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "purchase_order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    @JsonIgnore
    private PurchaseOrder purchaseOrder;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "item_code", length = 60)
    private String itemCode;

    @Column(name = "barcode", length = 100)
    private String barcode;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "description2", length = 255)
    private String description2;

    @Column(name = "product_group", length = 100)
    private String productGroup;

    @Column(name = "onhand")
    @Builder.Default
    private Integer onhand = 0;

    @Column(name = "suggest_qty")
    @Builder.Default
    private Integer suggestQty = 0;

    @Column(name = "qty")
    @Builder.Default
    private Double qty = 1.0;

    @Column(name = "cost")
    @Builder.Default
    private Double cost = 0.0;

    @Column(name = "discount")
    @Builder.Default
    private Double discount = 0.0;

    @Column(name = "uom", length = 50)
    private String uom;

    @Column(name = "total")
    @Builder.Default
    private Double total = 0.0;
}
