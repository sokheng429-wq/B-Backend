package com.bgroceries.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerGroupDto {

    private Long id;
    private String code;
    private String description;
    private String secondLanguage;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}