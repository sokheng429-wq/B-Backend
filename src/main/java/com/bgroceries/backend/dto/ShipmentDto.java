package com.bgroceries.backend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentDto {
    private Long id;
    private String shipCode;
    private LocalDateTime date;
    private String customer;
    private String phone;
    private BigDecimal balance;
    private BigDecimal amount;
    private String deliveryPerson;
    private String status;
    private String salesperson;
    private String reference;
    private String username;
    private String outlet;
    private String carrier;
    private String destination;
    private LocalDateTime createdAt;
}
