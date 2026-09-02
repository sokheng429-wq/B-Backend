package com.bgroceries.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleInvoiceDto {
    private Long id;
    private String invoiceCode;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private String soCode;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerAddress;
    private String salesperson;
    private String paymentTerm;
    private String outlet;
    private String location;
    private String templateName;
    private String status;
    private BigDecimal subTotal;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal taxPercent;
    private BigDecimal markupAmount;
    private BigDecimal grandTotal;
    private BigDecimal paidAmount;
    private BigDecimal balance;
    private BigDecimal exchangeRate;
    private BigDecimal grandTotalKhmer;
    private String barcode;
    private String username;
    private String note;
    private String paymentType;

    // Billing snapshot
    private String billingName;
    private String billingPhone;
    private String billingEmail;
    private String billingAddress;
    private String billingCity;
    private String billingTaxNo;

    // Shipping snapshot
    private String shippingRecipient;
    private String shippingPhone;
    private String shippingAddress;
    private String shippingMethod;
    private String trackingNo;

    @Builder.Default
    private List<SaleInvoiceItemDto> lines = new ArrayList<>();

    @Builder.Default
    private List<SaleInvoicePaymentDto> payments = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}