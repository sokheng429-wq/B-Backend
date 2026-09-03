package com.bgroceries.backend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebOrderDto {
    private Long id;
    private String code;
    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;
    private String salesperson;
    private String customerName;
    private String phone;
    private BigDecimal grandTotal;
    private BigDecimal balance;
    private String status;
    private String reference;
    private String username;
    private BigDecimal markup;
    private String outlet;
    private String channel;
    private String shippingAddress;
    @Builder.Default
    private List<WebOrderItemDto> items = new ArrayList<>();
    private LocalDateTime createdAt;
}
