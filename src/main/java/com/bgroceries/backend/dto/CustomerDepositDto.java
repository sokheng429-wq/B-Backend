package com.bgroceries.backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDepositDto {
    private Long id;
    private String code;
    private LocalDateTime depositDate;
    private BigDecimal amount;
    private Long customerId;
    private String customerName;
    private String contact;
    private String paymentType;
    private String reference;
    private String username;
    private String status;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
