package com.bgroceries.backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnInvoiceDto {
    private Long id;
    private String invoiceCode;
    private String applyToInvoice;
    private LocalDate returnDate;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerAddress;
    private BigDecimal grandTotal;
    private BigDecimal balance;
    private String taxCode;
    private String paymentTerm;
    private String salesperson;
    private BigDecimal markup;
    private String outlet;
    private String username;
    private String soCode;
    private String status;
    private String reason;
    private List<ReturnInvoiceItemDto> lines;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}