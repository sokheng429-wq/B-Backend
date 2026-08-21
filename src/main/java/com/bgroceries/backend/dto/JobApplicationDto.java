package com.bgroceries.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Combined request/response DTO for the JobApplication resource. Field names are
 * part of the API contract — do not rename.
 * <p>
 * Request (public apply + admin create context): jobId, fullName, email, phone,
 * linkedinUrl, coverLetter, resumeName, resumeData (base64), resumeContentType.
 * Response adds: id, jobTitle, status, createdAt. {@code resumeData} is echoed
 * back so the admin Applications view can build a data: URI.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobApplicationDto {

    /** Target job (optional on apply — the path id wins). */
    private Long jobId;

    private Long id;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Phone is required")
    private String phone;

    private String linkedinUrl;

    private String coverLetter;

    private String resumeName;

    private String resumeData;

    private String resumeContentType;

    /** Response-only: NEW / REVIEWED / ACCEPTED / REJECTED. */
    private String status;

    /** Response-only: title of the job this application targets. */
    private String jobTitle;

    /** Response-only. */
    private LocalDateTime createdAt;
}
