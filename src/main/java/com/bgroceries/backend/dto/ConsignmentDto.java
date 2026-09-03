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
public class ConsignmentDto {
    private Long id;
    private String code;
    private LocalDateTime consignmentDate;
    private LocalDateTime deliveryDate;
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
    private BigDecimal grandTotal;
    private BigDecimal balance;
    private String reference;
    private String username;
    private String note;

    private String billingName;
    private String billingPhone;
    private String billingEmail;
    private String billingAddress;

    private String shippingRecipient;
    private String shippingPhone;
    private String shippingAddress;
    private String shippingCourier;

    @Builder.Default
    private List<ConsignmentItemDto> items = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
