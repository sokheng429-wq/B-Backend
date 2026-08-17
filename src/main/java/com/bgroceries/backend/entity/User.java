package com.bgroceries.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "phone_number"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    /** Login identifier ("Users Name"). Nullable so existing Neon rows survive the migration. */
    @Column(unique = true, length = 50)
    private String username;

    /** Login via Gmail/email. */
    @Column(unique = true, length = 100)
    private String email;

    /** Login via Telegram. */
    @Column(unique = true, length = 100)
    private String telegram;

    /** Login via Facebook. */
    @Column(unique = true, length = 100)
    private String facebook;

    /** Nullable: social-media accounts (gmail/telegram/facebook) sign up without a phone. */
    @Column(name = "phone_number", nullable = true, unique = true, length = 20)
    private String phoneNumber;

    /** USER or ADMIN. Nullable for legacy rows; treat null as USER. */
    @Column(name = "role", length = 20)
    @Builder.Default
    private String role = "USER";

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

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
