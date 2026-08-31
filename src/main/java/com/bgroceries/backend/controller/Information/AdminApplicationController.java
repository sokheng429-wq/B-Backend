package com.bgroceries.backend.controller.Information;

import com.bgroceries.backend.dto.JobApplicationDto;
import com.bgroceries.backend.dto.request.StatusUpdateRequest;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.JobApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin job-applications report + status workflow, all wrapped in the standard
 * {@link ApiResponse} envelope. ROLE_ADMIN is enforced by the existing
 * SecurityConfig {@code /api/admin/**} matcher — no {@code @PreAuthorize} needed.
 */
@RestController
@RequestMapping("/api/admin/applications")
@RequiredArgsConstructor
public class AdminApplicationController {

    private final JobApplicationService jobApplicationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<JobApplicationDto>>> getAllApplications() {
        List<JobApplicationDto> applications = jobApplicationService.getAllApplications();
        return ResponseEntity.ok(ApiResponse.success("Applications retrieved successfully", applications));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobApplicationDto>> getApplicationById(@PathVariable Long id) {
        JobApplicationDto application = jobApplicationService.getApplicationById(id);
        return ResponseEntity.ok(ApiResponse.success("Application retrieved successfully", application));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<JobApplicationDto>> updateStatus(
            @PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        JobApplicationDto updated = jobApplicationService.updateStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Application status updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteApplication(@PathVariable Long id) {
        jobApplicationService.deleteApplication(id);
        return ResponseEntity.ok(ApiResponse.success("Application deleted successfully"));
    }
}
