package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.request.UpdateProfileRequest;
import com.bgroceries.backend.dto.response.AuthResponse;
import com.bgroceries.backend.dto.response.UserResponse;
import com.bgroceries.backend.entity.User;
import com.bgroceries.backend.exception.BadRequestException;
import com.bgroceries.backend.exception.ConflictException;
import com.bgroceries.backend.repository.UserRepository;
import com.bgroceries.backend.security.JwtUtil;
import com.bgroceries.backend.util.PhoneUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Profile updates for the authenticated user (Account Details page).
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /**
     * Applies a partial profile update. Returns a fresh {@link AuthResponse} so the
     * frontend keeps a valid token even when the phone (the JWT subject) changes.
     */
    @Transactional
    public AuthResponse updateProfile(User user, UpdateProfileRequest request) {
        if (request.getFullName() != null) {
            if (request.getFullName().isBlank()) {
                throw new BadRequestException("Full name cannot be empty");
            }
            user.setFullName(request.getFullName().trim());
        }

        if (request.getEmail() != null) {
            String email = request.getEmail().trim();
            if (email.isEmpty()) {
                user.setEmail(null);
            } else {
                userRepository.findByEmailIgnoreCase(email)
                        .filter(other -> !other.getId().equals(user.getId()))
                        .ifPresent(other -> {
                            throw new ConflictException("Email already in use");
                        });
                user.setEmail(email);
            }
        }

        if (request.getPhoneNumber() != null) {
            String phone = PhoneUtil.normalize(request.getPhoneNumber());
            if (phone == null || phone.isEmpty()) {
                user.setPhoneNumber(null);
            } else {
                userRepository.findByPhoneNumber(phone)
                        .filter(other -> !other.getId().equals(user.getId()))
                        .ifPresent(other -> {
                            throw new ConflictException("Phone number already in use");
                        });
                user.setPhoneNumber(phone);
            }
        }

        if (request.getDateOfBirth() != null) {
            if (request.getDateOfBirth().isBlank()) {
                user.setDateOfBirth(null);
            } else {
                try {
                    user.setDateOfBirth(LocalDate.parse(request.getDateOfBirth().trim()));
                } catch (DateTimeParseException e) {
                    throw new BadRequestException("Invalid date of birth (use yyyy-MM-dd)");
                }
            }
        }

        if (request.getGender() != null) {
            user.setGender(request.getGender().isBlank() ? null : request.getGender().trim());
        }

        if (request.getNationality() != null) {
            user.setNationality(request.getNationality().isBlank() ? null : request.getNationality().trim());
        }

        userRepository.save(user);
        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String subject = user.getPhoneNumber() != null ? user.getPhoneNumber() : user.getUsername();
        String role = user.getRole() != null ? user.getRole() : "USER";
        String token = jwtUtil.generateAccessToken(subject, user.getId(), role);

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .telegram(user.getTelegram())
                .facebook(user.getFacebook())
                .phoneNumber(user.getPhoneNumber())
                .role(role)
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .nationality(user.getNationality())
                .build();

        return AuthResponse.builder()
                .token(token)
                .user(userResponse)
                .build();
    }
}
