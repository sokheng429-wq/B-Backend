package com.bgroceries.backend.controller;

import com.bgroceries.backend.dto.JobDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin job management endpoints, all wrapped in the standard {@link ApiResponse}
 * envelope. ROLE_ADMIN is enforced by the existing SecurityConfig
 * {@code /api/admin/**} matcher — no {@code @PreAuthorize} needed.
 */
@RestController
@RequestMapping("/api/admin/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<JobDto>>> getAllJobs() {
        List<JobDto> jobs = jobService.getAllJobs();
        return ResponseEntity.ok(ApiResponse.success("Jobs retrieved successfully", jobs));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobDto>> getJobById(@PathVariable Long id) {
        JobDto job = jobService.getJobById(id);
        return ResponseEntity.ok(ApiResponse.success("Job retrieved successfully", job));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<JobDto>> createJob(@Valid @RequestBody JobDto dto) {
        JobDto created = jobService.createJob(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JobDto>> updateJob(@PathVariable Long id, @Valid @RequestBody JobDto dto) {
        JobDto updated = jobService.updateJob(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Job updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.ok(ApiResponse.success("Job deleted successfully"));
    }
}
