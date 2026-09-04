package com.bgroceries.backend.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnReceiptPODto {
    private Long id;
    private String returnPoCode;
    private String poCode;
    private String receiptPoCode;
    private LocalDate date;
    private String supplier;
    private Long supplierId;
    private Double amount;
    private String status;
    private String outlet;
    private String username;
    private String reason;
    private String reference;
    private String note;
    private List<ReturnReceiptPOItemDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
