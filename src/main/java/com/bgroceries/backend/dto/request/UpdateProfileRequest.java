package com.bgroceries.backend.dto.request;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Body for {@code PUT /api/users/me}. All fields are optional (partial update);
 * a present-but-empty value clears the field.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    private String fullName;

    @Email(message = "Invalid email address")
    private String email;

    private String phoneNumber;

    /** ISO {@code yyyy-MM-dd}, or blank/null to keep/clear. */
    private String dateOfBirth;

    private String gender;

    private String nationality;
}
