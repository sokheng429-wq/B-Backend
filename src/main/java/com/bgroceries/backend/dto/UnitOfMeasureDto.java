package com.bgroceries.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Combined request/response DTO for the UnitOfMeasure resource (admin
 * Stocks → Unit of Measure CRUD). Field names are part of the API contract —
 * do not rename. Only {@code description} is required; {@code code} is
 * auto-generated as UN-#### when omitted and {@code factor} is the optional
 * conversion factor relative to the base unit (e.g. kg = 1000).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitOfMeasureDto {

    private Long id;

    /** Optional on create — the backend generates UN-0001, UN-0002… when blank. */
    @Size(max = 50, message = "Code must be at most 50 characters")
    private String code;

    /** Primary (English) display description of the unit. */
    @NotBlank(message = "Description is required")
    @Size(max = 200, message = "Description must be at most 200 characters")
    private String description;

    /** Second-language (Khmer) display text. */
    @Size(max = 200, message = "Second language must be at most 200 characters")
    private String nameKh;

    /** Conversion factor relative to the base unit (e.g. kilogram = 1000). */
    private Double factor;

    private Boolean active;

    /** Read-only audit stamps — populated from the entity. */
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
