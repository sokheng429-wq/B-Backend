package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.request.SocialLoginRequest;
import com.bgroceries.backend.dto.response.AuthResponse;
import com.bgroceries.backend.entity.User;
import com.bgroceries.backend.exception.BadRequestException;
import com.bgroceries.backend.repository.UserRepository;
import com.bgroceries.backend.security.JwtUtil;
import com.bgroceries.backend.social.SocialProfile;
import com.bgroceries.backend.social.SocialVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("dev")
class AuthServiceSocialTest {

    @Autowired
    private UserRepository userRepository;

    private AuthService authService;
    private FakeVerifier verifier;

    @BeforeEach
    void setUp() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "test-secret-at-least-32-characters-long!!");
        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpirationMs", 86_400_000L);
        ReflectionTestUtils.setField(jwtUtil, "resetTokenExpirationMs", 600_000L);

        verifier = new FakeVerifier();
        authService = new AuthService(
                userRepository,
                new BCryptPasswordEncoder(),
                jwtUtil,
                null, // OTP flows are not exercised here
                List.of(verifier));
    }

    @Test
    void firstLoginCreatesLinkedAccount() {
        verifier.provider = "gmail";
        verifier.profile = new SocialProfile("gmail", "google-id-1", "newuser@gmail.com", "New User", null, null);

        AuthResponse response = authService.socialLogin(new SocialLoginRequest("gmail", null, "google-token"));

        User saved = userRepository.findByGoogleId("google-id-1").orElseThrow();
        assertThat(response.getUser().getId()).isEqualTo(saved.getId());
        assertThat(saved.getEmail()).isEqualTo("newuser@gmail.com");
        assertThat(saved.getGoogleId()).isEqualTo("google-id-1");
        assertThat(saved.getPhoneNumber()).isNull();
        assertThat(saved.getRole()).isEqualTo("USER");
    }

    @Test
    void existingEmailIsLinkedAndStampedWithProviderId() {
        User existing = userRepository.save(User.builder()
                .username("existing")
                .fullName("Existing User")
                .email("existing@gmail.com")
                .phoneNumber("+85512345678")
                .passwordHash(new BCryptPasswordEncoder().encode("pass123"))
                .role("USER")
                .enabled(true)
                .build());

        verifier.provider = "gmail";
        verifier.profile = new SocialProfile("gmail", "google-id-2", "EXISTING@GMAIL.COM", "Existing User", null, null);

        AuthResponse response = authService.socialLogin(new SocialLoginRequest("gmail", null, "token"));

        User linked = userRepository.findByGoogleId("google-id-2").orElseThrow();
        assertThat(linked.getId()).isEqualTo(existing.getId());
        assertThat(response.getUser().getId()).isEqualTo(existing.getId());
        assertThat(linked.getPhoneNumber()).isEqualTo("+85512345678");
    }

    @Test
    void existingProviderIdTakesPrecedenceOverEmail() {
        User existing = userRepository.save(User.builder()
                .username("socialuser")
                .fullName("Existing Social")
                .email(null)
                .googleId("google-id-3")
                .passwordHash(new BCryptPasswordEncoder().encode("random"))
                .role("USER")
                .enabled(true)
                .build());

        verifier.provider = "gmail";
        verifier.profile = new SocialProfile("gmail", "google-id-3", "someone-else@gmail.com", "Existing Social", null, null);

        AuthResponse response = authService.socialLogin(new SocialLoginRequest("gmail", null, "token"));

        assertThat(response.getUser().getId()).isEqualTo(existing.getId());
        User unchanged = userRepository.findById(existing.getId()).orElseThrow();
        assertThat(unchanged.getEmail()).isNull(); // email from the token must NOT be stamped
    }

    @Test
    void telegramProfileIsLinkedByProviderId() {
        verifier.provider = "telegram";
        verifier.profile = new SocialProfile("telegram", "48264086", null, "Brata Rifki", "bratarifki", null);

        AuthResponse response = authService.socialLogin(new SocialLoginRequest("telegram", null, "telegram-auth-json"));

        User saved = userRepository.findByTelegramId("48264086").orElseThrow();
        assertThat(saved.getTelegram()).isEqualTo("bratarifki");
        assertThat(saved.getEmail()).isNull();
        assertThat(response.getUser().getId()).isEqualTo(saved.getId());
    }

    @Test
    void missingTokenFallsBackToDemoAccount() {
        AuthResponse response = authService.socialLogin(new SocialLoginRequest("gmail", "", null));

        assertThat(response.getUser().getEmail()).isEqualTo("gmail.demo@bgroceries.demo");
    }

    @Test
    void unsupportedProviderWithTokenIsRejected() {
        verifier.provider = "gmail";

        assertThatThrownBy(() -> authService.socialLogin(new SocialLoginRequest("twitter", null, "token")))
                .isInstanceOf(BadRequestException.class);
    }

    /** Minimal mutable verifier double (no Mockito — class mocking is broken on this JDK 26 host). */
    private static class FakeVerifier implements SocialVerifier {
        String provider;
        SocialProfile profile;

        @Override
        public String provider() {
            return provider;
        }

        @Override
        public SocialProfile verify(String token) {
            return profile;
        }
    }
}
