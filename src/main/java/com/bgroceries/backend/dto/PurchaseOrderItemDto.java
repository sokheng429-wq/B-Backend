package com.bgroceries.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderItemDto {
    private Long id;
    private Long productId;
    private String itemCode;
    private String barcode;
    private String description;
    private String description2;
    private String productGroup;
    private Integer onhand;
    private Integer suggestQty;
    private Double qty;
    private Double cost;
    private Double discount;
    private String uom;
    private Double total;
}
