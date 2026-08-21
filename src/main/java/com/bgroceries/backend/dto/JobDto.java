package com.bgroceries.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Combined request/response DTO for the Job resource (admin CRUD + public
 * careers listing). Field names are part of the API contract — do not rename.
 * The multi-line fields are line-oriented: description line 1 = overview,
 * lines 2+ = responsibilities bullets; requirements/benefits lines = bullets.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobDto {

    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Department is required")
    private String department;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Type is required")
    private String type;

    private String salary;

    private String description;

    private String requirements;

    private String benefits;

    /** Read-only — populated from the entity (used as the admin "posted date"). */
    private LocalDateTime createdAt;
}
