package com.bgroceries.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgingInvoiceDto {
    private Long id;
    private String code;
    private LocalDate date;
    private LocalDate dueDate;
    private String customer;
    private String contactName;
    private String phone;
    private String status;
    private BigDecimal grandTotal;
    private BigDecimal balance;
    private String salesperson;
    private String customerGroup;

    // Computed aging indicators
    private Integer daysOverdue;
    private String agingType; // CURRENT, 1_30, 31_60, 61_90, 91_120, OVER_120
}
