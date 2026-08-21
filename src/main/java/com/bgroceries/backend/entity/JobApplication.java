package com.bgroceries.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A public job application. The resume is stored as base64 TEXT plus filename
 * and content type (like member photos); the admin Applications view reopens it
 * as a data URI. Status is a String limited to NEW / REVIEWED / ACCEPTED /
 * REJECTED (default NEW).
 */
@Entity
@Table(name = "job_application")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "phone", nullable = false, length = 30)
    private String phone;

    @Column(name = "linkedin_url", length = 300)
    private String linkedinUrl;

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    @Column(name = "resume_name", length = 255)
    private String resumeName;

    /** Base64-encoded resume file content, stored as TEXT (like member photos). */
    @Column(name = "resume_data", columnDefinition = "TEXT")
    private String resumeData;

    @Column(name = "resume_content_type", length = 100)
    private String resumeContentType;

    /** One of NEW / REVIEWED / ACCEPTED / REJECTED. */
    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private String status = "NEW";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
