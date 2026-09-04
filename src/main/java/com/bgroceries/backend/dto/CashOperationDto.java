package com.bgroceries.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashOperationDto {

    private Long id;
    private String code;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime transactionDate;

    private String type; // CASH_IN, CASH_OUT
    private String partyType; // CUSTOMER, SUPPLIER, OTHER
    private String partyName;
    private Double amount;
    private String outlet;
    private String status; // NON_VOIDED, VOIDED
    private String category;
    private String referenceNo;
    private String description;
    private String username;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
