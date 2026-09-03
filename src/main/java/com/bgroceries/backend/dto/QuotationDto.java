package com.bgroceries.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuotationDto {
    private Long id;
    private String code;
    private LocalDateTime quotationDate;
    private LocalDateTime expiredDate;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerAddress;
    private String salesperson;
    private String paymentTerm;
    private String outlet;
    private String templateName;
    private String status;
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

    private String billingName;
    private String billingPhone;
    private String billingEmail;
    private String billingAddress;
    private String billingCity;
    private String billingTaxNo;

    private String shippingName;
    private String shippingPhone;
    private String shippingAddress;
    private String shippingCity;

    @Builder.Default
    private List<QuotationItemDto> items = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
