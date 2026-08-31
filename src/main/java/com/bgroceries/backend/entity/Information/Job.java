package com.bgroceries.backend.entity.Information;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A job opening on the public Careers page, managed by admins.
 * The multi-line fields (description / requirements / benefits) are line-oriented:
 * description line 1 = job overview paragraph, lines 2+ = key responsibilities
 * bullets; requirements and benefits lines are rendered as bullet lists.
 */
@Entity
@Table(name = "job")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @Column(name = "location", nullable = false, length = 100)
    private String location;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    /** Salary display text (e.g. "$500 - $800"), nullable. */
    @Column(name = "salary", length = 100)
    private String salary;

    /** Line-oriented: line 1 = Job Overview, lines 2+ = Key Responsibilities bullets. Stored as TEXT. */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** Bullet list, one item per line. Stored as TEXT. */
    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;

    /** Bullet list, one item per line. Stored as TEXT. */
    @Column(name = "benefits", columnDefinition = "TEXT")
    private String benefits;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
