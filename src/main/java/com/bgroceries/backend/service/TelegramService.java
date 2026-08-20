package com.bgroceries.backend.service;

import com.bgroceries.backend.entity.LoginSession;
import com.bgroceries.backend.entity.User;
import com.bgroceries.backend.repository.LoginSessionRepository;
import com.bgroceries.backend.repository.UserRepository;
import com.bgroceries.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramService {

    private final LoginSessionRepository loginSessionRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public LoginSession createLoginSession() {
        String token = generateSecureToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

        LoginSession session = LoginSession.builder()
                .token(token)
                .status("PENDING")
                .expiresAt(expiresAt)
                .build();

        return loginSessionRepository.save(session);
    }

    @Transactional
    public void processStartCommand(Long telegramUserId, String telegramUsername, String telegramFirstName, String sessionToken) {
        LoginSession session = loginSessionRepository.findByToken(sessionToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid session token"));

        if (!session.getStatus().equals("PENDING")) {
            log.warn("Session {} already processed with status {}", sessionToken, session.getStatus());
            return;
        }

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            session.setStatus("EXPIRED");
            loginSessionRepository.save(session);
            throw new IllegalArgumentException("Session token expired");
        }

        User user = findOrCreateTelegramUser(telegramUserId, telegramUsername, telegramFirstName);

        String subject = user.getUsername() != null ? user.getUsername() : String.valueOf(user.getTelegramUserId());
        String jwt = jwtUtil.generateAccessToken(subject, user.getId(), user.getRole());

        session.setTelegramUserId(telegramUserId);
        session.setTelegramUsername(telegramUsername);
        session.setTelegramFirstName(telegramFirstName);
        session.setJwtToken(jwt);
        session.setStatus("COMPLETED");
        loginSessionRepository.save(session);

        log.info("Telegram login completed for user {} (session {})", telegramUserId, sessionToken);
    }

    public LoginSession getSession(String token) {
        return loginSessionRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
    }

    private User findOrCreateTelegramUser(Long telegramUserId, String telegramUsername, String telegramFirstName) {
        return userRepository.findByTelegramUserId(telegramUserId)
                .orElseGet(() -> createTelegramUser(telegramUserId, telegramUsername, telegramFirstName));
    }

    private User createTelegramUser(Long telegramUserId, String telegramUsername, String telegramFirstName) {
        String username = generateUniqueUsername(telegramUsername, telegramUserId);
        String fullName = telegramFirstName != null ? telegramFirstName : username;

        String randomPassword = Base64.getEncoder().encodeToString(
                secureRandom.generateSeed(32)
        );

        User user = User.builder()
                .telegramUserId(telegramUserId)
                .telegram(telegramUsername)
                .username(username)
                .fullName(fullName)
                .loginProvider("telegram")
                .passwordHash(passwordEncoder.encode(randomPassword))
                .role("USER")
                .enabled(true)
                .build();

        User saved = userRepository.save(user);
        log.info("Created new Telegram user: {} (ID: {})", username, telegramUserId);
        return saved;
    }

    private String generateUniqueUsername(String telegramUsername, Long telegramUserId) {
        if (telegramUsername != null && !userRepository.existsByUsername(telegramUsername)) {
            return telegramUsername;
        }

        String baseUsername = telegramUsername != null ? telegramUsername : "tg_user";
        String candidate = baseUsername + "_" + telegramUserId;

        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = baseUsername + "_" + telegramUserId + "_" + suffix;
            suffix++;
        }

        return candidate;
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
