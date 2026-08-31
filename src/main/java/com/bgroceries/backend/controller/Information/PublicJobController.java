package com.bgroceries.backend.controller.Information;

import com.bgroceries.backend.dto.JobDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public, unauthenticated careers listing for the Career page. PermitAll via the
 * existing SecurityConfig {@code /api/public/**} matcher. Returns the full
 * {@link JobDto} — job postings contain no personal data.
 */
@RestController
@RequestMapping("/api/public/jobs")
@RequiredArgsConstructor
public class PublicJobController {

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
}
