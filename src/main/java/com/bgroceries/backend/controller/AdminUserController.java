package com.bgroceries.backend.controller;

import com.bgroceries.backend.dto.AdminUserDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.entity.User;
import com.bgroceries.backend.exception.BadRequestException;
import com.bgroceries.backend.exception.ConflictException;
import com.bgroceries.backend.exception.NotFoundException;
import com.bgroceries.backend.repository.UserRepository;
import com.bgroceries.backend.util.PhoneUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin user management. Mounted under {@code /api/admin/**}, so
 * {@code SecurityConfig} already requires {@code ROLE_ADMIN} for every route here.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminUserDto>>> listUsers() {
        List<AdminUserDto> users = userRepository.findAll().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminUserDto>> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", toDto(findUser(id))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminUserDto>> createUser(@Valid @RequestBody AdminUserDto dto) {
        String username = requireNonBlank(dto.getUsername(), "Username is required").trim();
        String password = requireNonBlank(dto.getPassword(), "Password is required");
        String phone = dto.getPhoneNumber() == null ? null : PhoneUtil.normalize(dto.getPhoneNumber());
        String email = dto.getEmail() == null || dto.getEmail().isBlank() ? null : dto.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("Username already taken");
        }
        if (email != null && userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already registered");
        }
        if (phone != null && userRepository.existsByPhoneNumber(phone)) {
            throw new ConflictException("Phone number already registered");
        }

        User user = User.builder()
                .username(username)
                .fullName(dto.getFullName().trim())
                .email(email)
                .phoneNumber(phone)
                .passwordHash(passwordEncoder.encode(password))
                .role(normalizeRole(dto.getRole()))
                .enabled(dto.getEnabled() == null || dto.getEnabled())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", toDto(userRepository.save(user))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminUserDto>> updateUser(@PathVariable Long id,
                                                                 @Valid @RequestBody AdminUserDto dto) {
        User user = findUser(id);
        String phone = dto.getPhoneNumber() == null ? null : PhoneUtil.normalize(dto.getPhoneNumber());
        String email = dto.getEmail() == null || dto.getEmail().isBlank() ? null : dto.getEmail().trim().toLowerCase();

        if (email != null) {
            userRepository.findByEmailIgnoreCase(email)
                    .filter(other -> !other.getId().equals(id))
                    .ifPresent(other -> {
                        throw new ConflictException("Email already registered");
                    });
        }
        if (phone != null) {
            userRepository.findByPhoneNumber(phone)
                    .filter(other -> !other.getId().equals(id))
                    .ifPresent(other -> {
                        throw new ConflictException("Phone number already registered");
                    });
        }

        user.setFullName(dto.getFullName().trim());
        user.setEmail(email);
        user.setPhoneNumber(phone);
        user.setRole(normalizeRole(dto.getRole()));
        user.setEnabled(dto.getEnabled() == null || dto.getEnabled());

        return ResponseEntity.ok(ApiResponse.success("User updated successfully", toDto(userRepository.save(user))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id,
                                                        @AuthenticationPrincipal UserDetails principal) {
        User target = findUser(id);

        // Prevent an admin from deleting their own account (would lock them out).
        User self = userRepository.findByPhoneNumber(principal.getUsername())
                .or(() -> userRepository.findByUsernameIgnoreCase(principal.getUsername()))
                .orElse(null);
        if (self != null && self.getId().equals(id)) {
            throw new BadRequestException("You cannot delete your own account");
        }

        userRepository.delete(target);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private String normalizeRole(String role) {
        String r = role == null || role.isBlank() ? "USER" : role.trim().toUpperCase();
        // ADMIN = full access; STORE ("Online Store") = products-side access only;
        // USER = regular account. Ordering matters for the admin UI legend.
        if (!"ADMIN".equals(r) && !"STORE".equals(r) && !"USER".equals(r)) {
            throw new BadRequestException("Role must be ADMIN, STORE or USER");
        }
        return r;
    }

    private String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(message);
        }
        return value;
    }

    private AdminUserDto toDto(User user) {
        return AdminUserDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole() != null ? user.getRole() : "USER")
                .enabled(user.getEnabled())
                .loginProvider(user.getLoginProvider())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
