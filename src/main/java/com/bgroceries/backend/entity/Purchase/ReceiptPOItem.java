package com.bgroceries.backend.entity.Purchase;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "receipt_po_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptPOItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_po_id", nullable = false)
    @JsonIgnore
    private ReceiptPO receiptPO;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "code", length = 60)
    private String code;

    @Column(name = "barcode", length = 100)
    private String barcode;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "qty")
    @Builder.Default
    private Double qty = 1.0;

    @Column(name = "uom", length = 50)
    private String uom;

    @Column(name = "cost")
    @Builder.Default
    private Double cost = 0.0;

    @Column(name = "total")
    @Builder.Default
    private Double total = 0.0;
}
