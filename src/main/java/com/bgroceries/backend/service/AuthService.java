package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.request.LoginRequest;
import com.bgroceries.backend.dto.request.PhoneRequest;
import com.bgroceries.backend.dto.request.RegisterRequest;
import com.bgroceries.backend.dto.request.ResetPasswordRequest;
import com.bgroceries.backend.dto.request.SocialLoginRequest;
import com.bgroceries.backend.dto.request.VerifyOtpRequest;
import com.bgroceries.backend.dto.response.AuthResponse;
import com.bgroceries.backend.dto.response.OtpSentResponse;
import com.bgroceries.backend.dto.response.ResetTokenResponse;
import com.bgroceries.backend.dto.response.UserResponse;
import com.bgroceries.backend.entity.User;
import com.bgroceries.backend.enums.OtpPurpose;
import com.bgroceries.backend.exception.BadRequestException;
import com.bgroceries.backend.exception.ConflictException;
import com.bgroceries.backend.exception.NotFoundException;
import com.bgroceries.backend.exception.UnauthorizedException;
import com.bgroceries.backend.repository.UserRepository;
import com.bgroceries.backend.security.JwtUtil;
import com.bgroceries.backend.security.TokenActivityStore;
import com.bgroceries.backend.social.SocialProfile;
import com.bgroceries.backend.social.SocialVerifier;
import com.bgroceries.backend.util.PhoneUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;
    private final List<SocialVerifier> socialVerifiers;
    private final TokenActivityStore tokenActivityStore;

    @Value("${app.jwt.reset-token-expiration-ms}")
    private long resetTokenExpirationMs;

    // ---------- Register ----------

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        String phone = PhoneUtil.normalize(request.getPhoneNumber());
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByPhoneNumber(phone)) {
            throw new ConflictException("An account with this phone number already exists");
        }
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("This username is already taken");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("This email is already registered");
        }

        User user = User.builder()
                .username(username)
                .fullName(request.getFullName().trim())
                .email(email)
                .phoneNumber(phone)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .enabled(true)
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .nationality(request.getNationality())
                .build();

        userRepository.save(user);

        return buildAuthResponse(user);
    }

    // ---------- Login with username / email / telegram / facebook ----------

    public AuthResponse login(LoginRequest request) {
        User user = findUserByIdentifier(request.getIdentifier())
                .orElseThrow(() -> new UnauthorizedException("Invalid username/email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            // If this account was created via social login (Google / Facebook / Telegram)
            // and does not have a user-defined password yet, establish the password now
            // so the user can log in with their username and password seamlessly.
            boolean isSocialAccount = user.getGoogleId() != null
                    || user.getFacebookId() != null
                    || user.getTelegramId() != null
                    || (user.getLoginProvider() != null && !user.getLoginProvider().isBlank());

            if (isSocialAccount && request.getPassword() != null && !request.getPassword().isBlank()) {
                user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
                user.setLoginProvider(null);
                userRepository.save(user);
                return buildAuthResponse(user);
            }

            throw new UnauthorizedException("Invalid username/email or password");
        }

        return buildAuthResponse(user);
    }

    // ---------- Social login (Gmail / Telegram / Facebook) ----------

    /**
     * Real social login: when the request carries a {@code token}, the matching
     * {@link SocialVerifier} cryptographically verifies it and the account is
     * linked by provider id (or verified email) / created on first login.
     * Without a token, falls back to the simulated find-or-create demo behavior.
     */
    @Transactional
    public AuthResponse socialLogin(SocialLoginRequest request) {
        if (request.getToken() != null && !request.getToken().isBlank()) {
            SocialVerifier verifier = findSocialVerifier(request.getProvider());
            SocialProfile profile = verifier.verify(request.getToken());
            User user = findOrCreateSocialUser(profile);
            return buildAuthResponse(user);
        }

        // ----- Simulated demo path (no real OAuth handshake) -----
        String provider = request.getProvider() == null ? "" : request.getProvider().trim().toLowerCase();
        String identifier = request.getIdentifier() == null ? "" : request.getIdentifier().trim();

        User user;
        switch (provider) {
            case "gmail" -> {
                // Empty identifier -> one-click demo account; repeated clicks map to the same user.
                boolean demo = identifier.isEmpty();
                String email = demo ? "gmail.demo@bgroceries.demo" : identifier.toLowerCase();
                if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                    throw new BadRequestException("Invalid email address");
                }
                String demoName = demo ? "Google User" : null;
                user = userRepository.findByEmailIgnoreCase(email)
                        .orElseGet(() -> createSocialUser(demoName, email, null, null));
            }
            case "telegram" -> {
                boolean demo = identifier.isEmpty();
                String telegram = demo ? "telegram.demo" : identifier.replaceAll("^@", "");
                if (telegram.isEmpty()) {
                    throw new BadRequestException("Telegram handle is required");
                }
                String demoName = demo ? "Telegram User" : null;
                user = userRepository.findByTelegram(telegram)
                        .orElseGet(() -> createSocialUser(demoName, null, telegram, null));
            }
            case "facebook" -> {
                boolean demo = identifier.isEmpty();
                String facebook = demo ? "facebook.demo" : identifier.replaceAll("^@", "");
                if (facebook.isEmpty()) {
                    throw new BadRequestException("Facebook handle is required");
                }
                String demoName = demo ? "Facebook User" : null;
                user = userRepository.findByFacebook(facebook)
                        .orElseGet(() -> createSocialUser(demoName, null, null, facebook));
            }
            default -> throw new BadRequestException("Unsupported provider. Use gmail, telegram or facebook");
        }

        return buildAuthResponse(user);
    }

    private User createSocialUser(String fullName, String email, String telegram, String facebook) {
        String base = fullName != null ? fullName
                : email != null ? email.split("@")[0]
                : telegram != null ? telegram : facebook;

        User user = User.builder()
                .username(uniqueUsername(base))
                .fullName(toDisplayName(base))
                .email(email)
                .telegram(telegram)
                .facebook(facebook)
                // Social accounts have no real password; store an unguessable random one.
                .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role("USER")
                .enabled(true)
                .build();

        userRepository.save(user);
        return user;
    }

    // ---------- Verified social login: find-or-create from a SocialProfile ----------

    private SocialVerifier findSocialVerifier(String provider) {
        return socialVerifiers.stream()
                .filter(v -> v.provider().equalsIgnoreCase(provider))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Unsupported provider. Use gmail, telegram or facebook"));
    }

    private User findOrCreateSocialUser(SocialProfile profile) {
        User user = findByProviderId(profile);
        if (user == null && profile.email() != null) {
            // Provider-verified email linking (Google/Facebook only; Telegram sends no email).
            user = userRepository.findByEmailIgnoreCase(profile.email()).orElse(null);
            if (user != null) {
                stampProviderId(user, profile);
                userRepository.save(user);
            }
        }
        return user != null ? user : createSocialProfileUser(profile);
    }

    private User findByProviderId(SocialProfile profile) {
        return switch (profile.provider()) {
            case "gmail" -> userRepository.findByGoogleId(profile.providerId()).orElse(null);
            case "facebook" -> userRepository.findByFacebookId(profile.providerId()).orElse(null);
            case "telegram" -> userRepository.findByTelegramId(profile.providerId()).orElse(null);
            default -> null;
        };
    }

    private void stampProviderId(User user, SocialProfile profile) {
        switch (profile.provider()) {
            case "gmail" -> user.setGoogleId(profile.providerId());
            case "facebook" -> user.setFacebookId(profile.providerId());
            case "telegram" -> user.setTelegramId(profile.providerId());
            default -> throw new BadRequestException("Unsupported provider. Use gmail, telegram or facebook");
        }
    }

    private User createSocialProfileUser(SocialProfile profile) {
        String base = profile.fullName() != null ? profile.fullName()
                : profile.email() != null ? profile.email().split("@")[0]
                : profile.telegramUsername() != null ? profile.telegramUsername()
                : profile.provider() + "-" + profile.providerId();

        User user = User.builder()
                .username(uniqueUsername(base))
                .fullName(profile.fullName() != null ? profile.fullName() : toDisplayName(base))
                .email(profile.email())
                .telegram(profile.telegramUsername())
                .googleId("gmail".equals(profile.provider()) ? profile.providerId() : null)
                .facebookId("facebook".equals(profile.provider()) ? profile.providerId() : null)
                .telegramId("telegram".equals(profile.provider()) ? profile.providerId() : null)
                .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role("USER")
                .enabled(true)
                .build();

        userRepository.save(user);
        return user;
    }

    private String uniqueUsername(String base) {
        String sanitized = base.toLowerCase().replaceAll("[^a-z0-9._-]", "");
        if (sanitized.isEmpty()) {
            sanitized = "user";
        }
        String candidate = sanitized;
        int i = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = sanitized + (i++);
        }
        return candidate;
    }

    private String toDisplayName(String base) {
        StringBuilder sb = new StringBuilder();
        for (String part : base.replaceAll("[._@\\-+]+", " ").trim().split("\\s+")) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(" ");
            }
        }
        String name = sb.toString().trim();
        return name.isEmpty() ? "Social User" : name;
    }

    private Optional<User> findUserByIdentifier(String rawIdentifier) {
        String identifier = rawIdentifier == null ? "" : rawIdentifier.trim();
        if (identifier.isEmpty()) {
            return Optional.empty();
        }

        // Phone number still works for existing accounts.
        String phone = PhoneUtil.normalize(identifier);
        if (phone != null) {
            Optional<User> byPhone = userRepository.findByPhoneNumber(phone);
            if (byPhone.isPresent()) {
                return byPhone;
            }
        }

        return userRepository.findByUsernameIgnoreCase(identifier)
                .or(() -> userRepository.findByEmailIgnoreCase(identifier))
                .or(() -> userRepository.findByTelegram(identifier))
                .or(() -> userRepository.findByFacebook(identifier))
                .or(() -> userRepository.findByFullNameIgnoreCase(identifier));
    }

    // ---------- Login with OTP ----------

    @Transactional
    public OtpSentResponse sendLoginOtp(PhoneRequest request) {
        String phone = PhoneUtil.normalize(request.getPhoneNumber());

        if (!userRepository.existsByPhoneNumber(phone)) {
            throw new NotFoundException("No account found with this phone number. Please register first.");
        }

        String debugOtp = otpService.generateAndSend(phone, OtpPurpose.LOGIN);

        return OtpSentResponse.builder()
                .phoneNumber(phone)
                .expiresInSeconds(otpService.getExpiryMinutes() * 60)
                .debugOtp(debugOtp)
                .build();
    }

    @Transactional
    public AuthResponse verifyLoginOtp(VerifyOtpRequest request) {
        String phone = PhoneUtil.normalize(request.getPhoneNumber());

        otpService.verify(phone, request.getOtp(), OtpPurpose.LOGIN);

        User user = userRepository.findByPhoneNumber(phone)
                .orElseThrow(() -> new NotFoundException("No account found with this phone number"));

        return buildAuthResponse(user);
    }

    // ---------- Forgot password ----------

    @Transactional
    public OtpSentResponse sendForgotPasswordOtp(PhoneRequest request) {
        String phone = PhoneUtil.normalize(request.getPhoneNumber());

        if (!userRepository.existsByPhoneNumber(phone)) {
            throw new NotFoundException("No account found with this phone number");
        }

        String debugOtp = otpService.generateAndSend(phone, OtpPurpose.RESET_PASSWORD);

        return OtpSentResponse.builder()
                .phoneNumber(phone)
                .expiresInSeconds(otpService.getExpiryMinutes() * 60)
                .debugOtp(debugOtp)
                .build();
    }

    @Transactional
    public ResetTokenResponse verifyForgotPasswordOtp(VerifyOtpRequest request) {
        String phone = PhoneUtil.normalize(request.getPhoneNumber());

        otpService.verify(phone, request.getOtp(), OtpPurpose.RESET_PASSWORD);

        String resetToken = jwtUtil.generateResetToken(phone);

        return ResetTokenResponse.builder()
                .resetToken(resetToken)
                .expiresInSeconds((int) (resetTokenExpirationMs / 1000))
                .build();
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        if (!jwtUtil.isTokenValid(request.getResetToken())) {
            throw new UnauthorizedException("Reset link has expired. Please start the process again.");
        }

        Claims claims = jwtUtil.parseClaims(request.getResetToken());

        if (!"RESET".equals(claims.get("type", String.class))) {
            throw new UnauthorizedException("Invalid reset token");
        }

        String phone = claims.getSubject();

        User user = userRepository.findByPhoneNumber(phone)
                .orElseThrow(() -> new NotFoundException("No account found with this phone number"));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // ---------- Helpers ----------

    // ---------- Logout ----------

    /**
     * Explicit logout: evicts the token from the activity store so that any
     * subsequent request carrying it will be treated as a session timeout.
     */
    public void logout(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            tokenActivityStore.evict(bearerToken.substring(7));
        }
    }

    // ---------- Helpers ----------

    private AuthResponse buildAuthResponse(User user) {
        // JWT subject: phone if present, otherwise username (social accounts may have no phone).
        String subject = user.getPhoneNumber() != null ? user.getPhoneNumber() : user.getUsername();
        String role = user.getRole() != null ? user.getRole() : "USER";
        String token = jwtUtil.generateAccessToken(subject, user.getId(), role);

        // Register the freshly-issued token in the activity store so that the
        // inactivity timer starts ticking from the moment of login.
        tokenActivityStore.register(token);

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
