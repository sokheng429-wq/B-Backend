package com.bgroceries.backend.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentMethodDto {
    private Long id;
    private String code;
    private String description;
    private String secondLanguage;
    private String costProration;
    private Boolean active;
    @Builder.Default
    private List<Long> tariffIds = new ArrayList<>();
    @Builder.Default
    private List<ShipmentTariffDto> tariffs = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
