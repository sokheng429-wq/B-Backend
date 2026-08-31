package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.JobDto;
import com.bgroceries.backend.entity.Information.Job;
import com.bgroceries.backend.exception.ConflictException;
import com.bgroceries.backend.exception.NotFoundException;
import com.bgroceries.backend.repository.JobApplicationRepository;
import com.bgroceries.backend.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD for the Job resource. Jobs with existing applications cannot be deleted
 * (ConflictException) — applications are never cascade-deleted.
 */
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;

    @Transactional(readOnly = true)
    public List<JobDto> getAllJobs() {
        return jobRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public JobDto getJobById(Long id) {
        return toDto(findJob(id));
    }

    @Transactional
    public JobDto createJob(JobDto dto) {
        Job job = Job.builder()
                .title(dto.getTitle().trim())
                .department(dto.getDepartment().trim())
                .location(dto.getLocation().trim())
                .type(dto.getType().trim())
                .salary(dto.getSalary())
                .description(dto.getDescription())
                .requirements(dto.getRequirements())
                .benefits(dto.getBenefits())
                .build();
        return toDto(jobRepository.save(job));
    }

    @Transactional
    public JobDto updateJob(Long id, JobDto dto) {
        Job job = findJob(id);
        job.setTitle(dto.getTitle().trim());
        job.setDepartment(dto.getDepartment().trim());
        job.setLocation(dto.getLocation().trim());
        job.setType(dto.getType().trim());
        job.setSalary(dto.getSalary());
        job.setDescription(dto.getDescription());
        job.setRequirements(dto.getRequirements());
        job.setBenefits(dto.getBenefits());
        return toDto(jobRepository.save(job));
    }

    @Transactional
    public void deleteJob(Long id) {
        Job job = findJob(id);
        if (jobApplicationRepository.countByJobId(id) > 0) {
            throw new ConflictException("Cannot delete job with existing applications");
        }
        jobRepository.delete(job);
    }

    private Job findJob(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Job not found"));
    }

    private JobDto toDto(Job job) {
        return JobDto.builder()
                .id(job.getId())
                .title(job.getTitle())
                .department(job.getDepartment())
                .location(job.getLocation())
                .type(job.getType())
                .salary(job.getSalary())
                .description(job.getDescription())
                .requirements(job.getRequirements())
                .benefits(job.getBenefits())
                .createdAt(job.getCreatedAt())
                .build();
    }
}
