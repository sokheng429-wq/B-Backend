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
public class ArCollectionDto {
    private Long id;
    private String code;
    private LocalDateTime paymentDate;
    private BigDecimal rate;
    private BigDecimal paidAmount;
    private BigDecimal balance;
    private BigDecimal currentAmount;
    private BigDecimal remainAmount;
    private Long customerId;
    private String customer;
    private String contact;
    private String user;
    private String status;
    private String note;
    private String applyMethod;
    private String paymentType;
    private String authorizationNote;
    private List<ArCollectionInvoiceDto> invoices;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
