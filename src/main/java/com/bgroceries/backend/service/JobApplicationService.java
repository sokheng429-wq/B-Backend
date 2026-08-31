package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.JobApplicationDto;
import com.bgroceries.backend.entity.Information.Job;
import com.bgroceries.backend.entity.Information.JobApplication;
import com.bgroceries.backend.exception.BadRequestException;
import com.bgroceries.backend.exception.NotFoundException;
import com.bgroceries.backend.repository.JobApplicationRepository;
import com.bgroceries.backend.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Public apply + admin report workflow for job applications. A new application
 * always starts in status NEW. Status transitions are validated against the
 * fixed set NEW / REVIEWED / ACCEPTED / REJECTED.
 */
@Service
@RequiredArgsConstructor
public class JobApplicationService {

    /** Hard cap for the base64 resume payload (chars) — TEXT column, like member photos. */
    public static final int MAX_RESUME_DATA_LENGTH = 5_000_000;

    private static final Set<String> VALID_STATUSES = Set.of("NEW", "REVIEWED", "ACCEPTED", "REJECTED");

    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;

    @Transactional
    public JobApplicationDto apply(Long jobId, JobApplicationDto dto) {
        Job job = findJob(jobId);
        validateResumeData(dto.getResumeData());

        JobApplication application = JobApplication.builder()
                .job(job)
                .fullName(dto.getFullName().trim())
                .email(dto.getEmail().trim())
                .phone(dto.getPhone().trim())
                .linkedinUrl(dto.getLinkedinUrl())
                .coverLetter(dto.getCoverLetter())
                .resumeName(dto.getResumeName())
                .resumeData(dto.getResumeData())
                .resumeContentType(dto.getResumeContentType())
                .status("NEW")
                .build();
        return toDto(jobApplicationRepository.save(application));
    }

    @Transactional(readOnly = true)
    public List<JobApplicationDto> getAllApplications() {
        return jobApplicationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public JobApplicationDto getApplicationById(Long id) {
        return toDto(findApplication(id));
    }

    @Transactional
    public JobApplicationDto updateStatus(Long id, String status) {
        if (status == null || !VALID_STATUSES.contains(status)) {
            throw new BadRequestException("Invalid status; must be one of NEW, REVIEWED, ACCEPTED, REJECTED");
        }
        JobApplication application = findApplication(id);
        application.setStatus(status);
        return toDto(jobApplicationRepository.save(application));
    }

    @Transactional
    public void deleteApplication(Long id) {
        jobApplicationRepository.delete(findApplication(id));
    }

    private Job findJob(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Job not found"));
    }

    private JobApplication findApplication(Long id) {
        return jobApplicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application not found"));
    }

    private void validateResumeData(String resumeData) {
        if (resumeData != null && resumeData.length() > MAX_RESUME_DATA_LENGTH) {
            throw new BadRequestException("Resume file is too large (max ~5MB)");
        }
    }

    private JobApplicationDto toDto(JobApplication application) {
        return JobApplicationDto.builder()
                .id(application.getId())
                .jobId(application.getJob().getId())
                .jobTitle(application.getJob().getTitle())
                .fullName(application.getFullName())
                .email(application.getEmail())
                .phone(application.getPhone())
                .linkedinUrl(application.getLinkedinUrl())
                .coverLetter(application.getCoverLetter())
                .resumeName(application.getResumeName())
                .resumeData(application.getResumeData())
                .resumeContentType(application.getResumeContentType())
                .status(application.getStatus())
                .createdAt(application.getCreatedAt())
                .build();
    }
}
