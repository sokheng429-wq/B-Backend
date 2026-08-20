package com.bgroceries.backend.dto.response;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String fullName;
    private String username;
    private String email;
    private String telegram;
    private String facebook;
    private String phoneNumber;
    private String role;
    private LocalDate dateOfBirth;
    private String gender;
    private String nationality;

    /** Which OAuth provider was used for login: "google", "facebook", "telegram", or null for password login */
    private String loginProvider;
}
