package com.bgroceries.backend.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderDto {
    private Long id;
    private String code;
    private LocalDate date;
    private LocalDate requireDate;
    private String purchasePerson;
    private String supplier;
    private Long supplierId;
    private String phone;
    private Double grandTotal;
    private Double balance;
    private String reference;
    private LocalDate voidedDate;
    private String soCode;
    private String status;
    private String username;
    private String outlet;
    private String paymentTerm;
    private String shipmentMethod;
    private String templateName;
    private String note;
    private Double subAmount;
    private Double discountPercent;
    private Double discountAmount;
    private Double taxAmount;
    private String billingAddress;
    private String shippingAddress;
    private String carrier;
    private String trackingNumber;

    @Builder.Default
    private List<PurchaseOrderItemDto> items = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
