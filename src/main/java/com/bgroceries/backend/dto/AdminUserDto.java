package com.bgroceries.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for admin user management. On create, {@code password} is required;
 * on update it is ignored (passwords are reset via the forgot-password flow).
 * {@code username} is the immutable login identifier — not editable.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDto {

    private Long id;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String username;

    private String email;

    private String phoneNumber;

    private String role;

    private Boolean enabled;

    private String loginProvider;

    private LocalDateTime createdAt;

    /** Only used when creating a user. */
    private String password;
}
