package com.bgroceries.backend.controller;

import com.bgroceries.backend.dto.request.UpdateProfileRequest;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.dto.response.AuthResponse;
import com.bgroceries.backend.dto.response.UserResponse;
import com.bgroceries.backend.entity.User;
import com.bgroceries.backend.exception.NotFoundException;
import com.bgroceries.backend.repository.UserRepository;
import com.bgroceries.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    /** Get the authenticated user's profile information. */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal UserDetails principal) {

        // JWT subject: phone number if present, otherwise the username (social accounts).
        User user = userRepository.findByPhoneNumber(principal.getUsername())
                .or(() -> userRepository.findByUsernameIgnoreCase(principal.getUsername()))
                .orElseThrow(() -> new NotFoundException("User not found"));

        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .telegram(user.getTelegram())
                .facebook(user.getFacebook())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .nationality(user.getNationality())
                .loginProvider(user.getLoginProvider())  // Include OAuth provider info
                .build();

        return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully", response));
    }

    /** Update the authenticated user's own profile (Account Details page). */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UpdateProfileRequest request) {

        // JWT subject: phone number if present, otherwise the username (social accounts).
        User user = userRepository.findByPhoneNumber(principal.getUsername())
                .or(() -> userRepository.findByUsernameIgnoreCase(principal.getUsername()))
                .orElseThrow(() -> new NotFoundException("User not found"));

        AuthResponse response = userService.updateProfile(user, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }
}
