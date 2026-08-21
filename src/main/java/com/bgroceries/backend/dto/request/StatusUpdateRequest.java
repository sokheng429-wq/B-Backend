package com.bgroceries.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request body for PATCH /api/admin/applications/{id}/status — {"status": "REVIEWED"}. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdateRequest {

    @NotBlank(message = "Status is required")
    private String status;
}
