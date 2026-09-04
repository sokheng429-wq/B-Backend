package com.bgroceries.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequisitionItemDto {
    private Long id;
    private Long productId;
    private String code;
    private String barcode;
    private String description;
    private Double requisitionQty;
    private String uom;
    private Double cost;
    private Double total;
}
