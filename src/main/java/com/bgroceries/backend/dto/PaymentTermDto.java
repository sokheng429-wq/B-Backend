package com.bgroceries.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTermDto {
    private Long id;
    private String code;
    private String description;
    private String secondLanguage;
    private Integer days;
    private Boolean active;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
