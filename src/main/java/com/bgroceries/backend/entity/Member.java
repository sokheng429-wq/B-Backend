package com.bgroceries.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A team member of B'Groceries. Holds the identity/filter fields; the
 * {@link MemberDetail} side carries the extended profile (phone, dates, ...).
 */
@Entity
@Table(name = "member", uniqueConstraints = @UniqueConstraint(columnNames = "member_code"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Stable business code (e.g. M-001). Unique, case-insensitively. */
    @Column(name = "member_code", nullable = false, unique = true, length = 50)
    private String memberCode;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(length = 100)
    private String position;

    @Column
    private Integer rank;

    @Column(length = 100)
    private String department;

    @Column(length = 100)
    private String category;

    /** Photo for the member (a URL or a data: URL), nullable. Stored as TEXT for large images. */
    @Column(name = "photo_url", columnDefinition = "TEXT")
    private String photoUrl;

    /** Owning side is {@link MemberDetail#getMember()}; deleting a member removes the detail. */
    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private MemberDetail memberDetail;

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
