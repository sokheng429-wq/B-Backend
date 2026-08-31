package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Information.JobApplication;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    /** Newest first; job eagerly fetched so jobTitle is available without N+1. */
    @EntityGraph(attributePaths = "job")
    List<JobApplication> findAllByOrderByCreatedAtDesc();

    @Override
    @EntityGraph(attributePaths = "job")
    Optional<JobApplication> findById(Long id);

    long countByJobId(Long jobId);
}
