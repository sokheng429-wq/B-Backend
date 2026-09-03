package com.bgroceries.backend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnShipmentDto {
    private Long id;
    private String returnShipCode;
    private String soCode;
    private LocalDateTime date;
    private String customer;
    private String deliveryPerson;
    private BigDecimal amount;
    private String status;
    private String outlet;
    private String username;
    private LocalDateTime createdAt;
}
