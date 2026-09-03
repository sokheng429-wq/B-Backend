package com.bgroceries.backend.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebOrderItemDto {
    private Long id;
    private Long productId;
    private String productCode;
    private String description;
    private BigDecimal qty;
    private BigDecimal price;
    private BigDecimal total;
}
