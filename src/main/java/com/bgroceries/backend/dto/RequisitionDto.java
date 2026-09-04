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
public class RequisitionDto {
    private Long id;
    private String code;
    private LocalDate date;
    private LocalDate requireDate;
    private String templateName;
    private String requisitionType;
    private Double requisitionAmount;
    private String reference;
    private String referenceCode;
    private String userName;
    private String status;
    private String note;
    private List<RequisitionItemDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
