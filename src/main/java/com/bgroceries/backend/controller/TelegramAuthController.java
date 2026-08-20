package com.bgroceries.backend.controller;

import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.dto.response.UserResponse;
import com.bgroceries.backend.entity.LoginSession;
import com.bgroceries.backend.entity.User;
import com.bgroceries.backend.repository.UserRepository;
import com.bgroceries.backend.service.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/telegram")
@RequiredArgsConstructor
@Slf4j
public class TelegramAuthController {

    private final TelegramService telegramService;
    private final UserRepository userRepository;

    @PostMapping("/init")
    public ResponseEntity<ApiResponse<Map<String, String>>> initLogin() {
        LoginSession session = telegramService.createLoginSession();

        Map<String, String> response = new HashMap<>();
        response.put("token", session.getToken());
        response.put("expiresAt", session.getExpiresAt().toString());

        log.info("Telegram login session initiated: {}", session.getToken());
        return ResponseEntity.ok(ApiResponse.success("Login session created", response));
    }

    @GetMapping("/status/{token}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkStatus(@PathVariable String token) {
        try {
            LoginSession session = telegramService.getSession(token);

            Map<String, Object> response = new HashMap<>();
            response.put("status", session.getStatus());

            if ("COMPLETED".equals(session.getStatus())) {
                // Get the full user object
                User user = userRepository.findByTelegramUserId(session.getTelegramUserId())
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));

                // Build user response matching your existing AuthResponse format
                UserResponse userResponse = UserResponse.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .phoneNumber(user.getPhoneNumber())
                        .telegram(user.getTelegram())
                        .role(user.getRole())
                        .build();

                response.put("token", session.getJwtToken());
                response.put("tokenType", "Bearer");
                response.put("user", userResponse);

                // Also include for backward compatibility
                response.put("jwt", session.getJwtToken());
                response.put("telegramUserId", session.getTelegramUserId());
                response.put("telegramUsername", session.getTelegramUsername());
            } else if ("EXPIRED".equals(session.getStatus()) ||
                       session.getExpiresAt().isBefore(LocalDateTime.now())) {
                response.put("status", "EXPIRED");
            }

            return ResponseEntity.ok(ApiResponse.success("Status retrieved", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
}
