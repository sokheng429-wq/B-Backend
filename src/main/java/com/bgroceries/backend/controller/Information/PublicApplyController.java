package com.bgroceries.backend.controller.Information;

import com.bgroceries.backend.dto.JobApplicationDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.JobApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public, unauthenticated job-application submission. PermitAll via the existing
 * SecurityConfig {@code /api/public/**} matcher. The job id in the path wins
 * over any jobId in the body. New applications always start in status NEW.
 */
@RestController
@RequestMapping("/api/public/jobs/{id}/apply")
@RequiredArgsConstructor
public class PublicApplyController {

    private final JobApplicationService jobApplicationService;

    @PostMapping
    public ResponseEntity<ApiResponse<JobApplicationDto>> apply(
            @PathVariable Long id, @Valid @RequestBody JobApplicationDto dto) {
        JobApplicationDto created = jobApplicationService.apply(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Application submitted successfully", created));
    }
}
