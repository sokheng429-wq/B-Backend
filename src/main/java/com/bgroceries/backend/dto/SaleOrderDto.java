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
public class SaleOrderDto {
    private Long id;
    private String code;
    private String quoteCode;
    private String poCode;
    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String salesperson;
    private String paymentTerm;
    private String outlet;
    private String templateName;
    private String status;
    private BigDecimal creditLimit;
    private BigDecimal availableCredit;
    private BigDecimal subAmount;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal markupAmount;
    private BigDecimal grandTotal;
    private BigDecimal balance;
    private String reference;
    private String username;
    private String note;
    private String relatedPurchaseOrder;
    @Builder.Default
    private List<SaleOrderItemDto> items = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
