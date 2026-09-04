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
public class ReceiptPODto {
    private Long id;
    private String receiptPoCode;
    private String poCode;
    private LocalDate date;
    private String supplier;
    private Long supplierId;
    private Double balance;
    private Double amount;
    private Double freightAmount;
    private Double qty;
    private String status;
    private String outlet;
    private String shipment;
    private String username;
    private String reference;
    private String note;

    @Builder.Default
    private List<ReceiptPOItemDto> items = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
