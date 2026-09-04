package com.bgroceries.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnReceiptPOItemDto {
    private Long id;
    private Long productId;
    private String code;
    private String barcode;
    private String description;
    private Double qty;
    private String uom;
    private Double cost;
    private Double total;
}
