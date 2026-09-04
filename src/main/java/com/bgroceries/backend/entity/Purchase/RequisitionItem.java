package com.bgroceries.backend.entity.Purchase;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "requisition_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequisitionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requisition_id", nullable = false)
    @JsonIgnore
    private Requisition requisition;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "code", length = 60)
    private String code;

    @Column(name = "barcode", length = 100)
    private String barcode;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "requisition_qty")
    @Builder.Default
    private Double requisitionQty = 1.0;

    @Column(name = "uom", length = 50)
    private String uom;

    @Column(name = "cost")
    @Builder.Default
    private Double cost = 0.0;

    @Column(name = "total")
    @Builder.Default
    private Double total = 0.0;
}
