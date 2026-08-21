package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    /** Newest job first (createdAt is set by the entity's @PrePersist). */
    List<Job> findAllByOrderByCreatedAtDesc();
}
