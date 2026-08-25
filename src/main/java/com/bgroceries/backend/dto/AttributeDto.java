package com.bgroceries.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Combined request/response DTO for the Attribute resource (admin
 * Stocks → Attributes CRUD). Field names are part of the API contract — do not
 * rename. Only {@code description} is required; {@code code} is
 * auto-generated as AT-#### when omitted.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeDto {

    private Long id;

    /** Optional on create — the backend generates AT-0001, AT-0002… when blank. */
    @Size(max = 50, message = "Code must be at most 50 characters")
    private String code;

    /** Primary (English) attribute name, e.g. "Color". */
    @NotBlank(message = "Description is required")
    @Size(max = 200, message = "Description must be at most 200 characters")
    private String description;

    /** Second-language (Khmer) display text. */
    @Size(max = 200, message = "Second language must be at most 200 characters")
    private String nameKh;

    /** Free-text type tag: Text / Number / Color / Size / Flavor… */
    @Size(max = 50, message = "Type must be at most 50 characters")
    private String type;

    /** Comma-separated allowed values, e.g. "Small, Medium, Large". */
    @Size(max = 1000, message = "Values must be at most 1000 characters")
    private String values;

    private Boolean active;

    /** Read-only audit stamps — populated from the entity. */
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
