package com.bgroceries.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentTariffDto {
    private Long id;
    private String code;
    private String description;
    private String secondLanguage;
    private Long supplierId;
    private String supplier;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
