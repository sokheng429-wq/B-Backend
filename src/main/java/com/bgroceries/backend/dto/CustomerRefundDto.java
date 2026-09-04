package com.bgroceries.backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRefundDto {
    private Long id;
    private String code;
    private LocalDateTime paymentDate;
    private BigDecimal rate;
    private BigDecimal paidAmount;
    private BigDecimal balance;
    private Long customerId;
    private String partner;
    private String contact;
    private String phone;
    private String username;
    private String status;
    private String note;
    private String paymentType;
    private String authorizationNote;
    private List<CustomerRefundInvoiceDto> invoices;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
