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

import java.time.LocalDate;
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

    /**
     * Login identifier ("Users Name"). Nullable so existing Neon rows survive the
     * migration.
     */
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

    /** Stable Google account id from a verified ID token (nullable). */
    @Column(name = "google_id", unique = true, length = 100)
    private String googleId;

    /** Stable Facebook account id from a verified access token (nullable). */
    @Column(name = "facebook_id", unique = true, length = 100)
    private String facebookId;

    /** Stable Telegram account id from a verified Login Widget auth (nullable). */
    @Column(name = "telegram_id", unique = true, length = 100)
    private String telegramId;

    /** Numeric Telegram user ID from bot login (nullable). */
    @Column(name = "telegram_user_id", unique = true)
    private Long telegramUserId;

    /**
     * Track which OAuth provider was used for login: "google", "facebook",
     * "telegram", or null for password login
     */
    @Column(name = "login_provider", length = 20)
    private String loginProvider;

    /**
     * Nullable: social-media accounts (gmail/telegram/facebook) sign up without a
     * phone.
     */
    @Column(name = "phone_number", nullable = true, unique = true, length = 20)
    private String phoneNumber;

    /**
     * Optional profile details (Account Details page). Nullable for existing rows.
     */
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 20)
    private String gender;

    @Column(name = "nationality", length = 100)
    private String nationality;

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
