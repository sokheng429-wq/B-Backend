package com.bgroceries.backend.service;

import com.bgroceries.backend.entity.OtpCode;
import com.bgroceries.backend.entity.PasswordResetOtp;
import com.bgroceries.backend.entity.User;
import com.bgroceries.backend.enums.OtpPurpose;
import com.bgroceries.backend.exception.BadRequestException;
import com.bgroceries.backend.exception.NotFoundException;
import com.bgroceries.backend.repository.OtpCodeRepository;
import com.bgroceries.backend.repository.PasswordResetOtpRepository;
import com.bgroceries.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpCodeRepository otpCodeRepository;
    private final PasswordResetOtpRepository passwordResetOtpRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SmsService smsService;
    private final EmailService emailService;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Value("${app.otp.length:6}")
    private int otpLength;

    @Value("${app.otp.expiry-minutes:5}")
    private int expiryMinutes;

    @Value("${app.otp.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.otp.expose-in-response:false}")
    private boolean exposeInResponse;

    /**
     * Generates a new OTP, stores its hash, sends it by SMS, and returns
     * the raw code only if app.otp.expose-in-response=true (dev/testing).
     */
    @Transactional
    public String generateAndSend(String phoneNumber, OtpPurpose purpose) {
        String code = generateNumericCode();

        OtpCode otpCode = OtpCode.builder()
                .phoneNumber(phoneNumber)
                .codeHash(passwordEncoder.encode(code))
                .purpose(purpose)
                .expiresAt(LocalDateTime.now().plusMinutes(expiryMinutes))
                .used(false)
                .verified(false)
                .attempts(0)
                .build();

        otpCodeRepository.save(otpCode);
        smsService.sendOtp(phoneNumber, code);

        return exposeInResponse ? code : null;
    }

    @Transactional
    public void verify(String phoneNumber, String code, OtpPurpose purpose) {
        OtpCode otpCode = otpCodeRepository
                .findTopByPhoneNumberAndPurposeAndUsedFalseOrderByCreatedAtDesc(phoneNumber, purpose)
                .orElseThrow(() -> new BadRequestException("No active OTP found. Please request a new code."));

        if (otpCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP has expired. Please request a new code.");
        }

        if (otpCode.getAttempts() >= maxAttempts) {
            throw new BadRequestException("Too many incorrect attempts. Please request a new code.");
        }

        if (!passwordEncoder.matches(code, otpCode.getCodeHash())) {
            otpCode.setAttempts(otpCode.getAttempts() + 1);
            otpCodeRepository.save(otpCode);
            throw new BadRequestException("Invalid OTP code.");
        }

        otpCode.setUsed(true);
        otpCode.setVerified(true);
        otpCodeRepository.save(otpCode);
    }

    public int getExpiryMinutes() {
        return expiryMinutes;
    }

    // ---------- Email-based forgot-password OTP (PasswordResetOtp) ----------

    /**
     * Email variant of {@link #generateAndSend(String, OtpPurpose)}. Invalidate any
     * previous code for this address, store a BCrypt hash of the new one, and
     * deliver it via {@link EmailService}. Rejects unregistered emails up front
     * (same behavior as the phone flow) so the user knows immediately instead of
     * failing confusingly at the reset step.
     */
    @Transactional
    public String generateAndSendOtp(String email) {
        String normalized = normalizeEmail(email);

        if (!userRepository.existsByEmail(normalized)) {
            throw new NotFoundException("No account found with this email address");
        }

        String code = generateNumericCode();
        passwordResetOtpRepository.deleteByEmail(normalized);

        PasswordResetOtp otp = PasswordResetOtp.builder()
                .email(normalized)
                .codeHash(passwordEncoder.encode(code))
                .expiresAt(LocalDateTime.now().plusMinutes(expiryMinutes))
                .used(false)
                .verified(false)
                .attempts(0)
                .build();
        passwordResetOtpRepository.save(otp);

        emailService.sendOtp(normalized, code);

        return exposeInResponse ? code : null;
    }

    @Transactional
    public boolean verifyOtp(String email, String code) {
        String normalized = normalizeEmail(email);

        PasswordResetOtp otp = passwordResetOtpRepository
                .findTopByEmailOrderByCreatedAtDesc(normalized)
                .orElse(null);

        if (otp == null || otp.getUsed()) {
            return false;
        }
        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }
        if (otp.getAttempts() >= maxAttempts) {
            return false;
        }
        if (!passwordEncoder.matches(code, otp.getCodeHash())) {
            otp.setAttempts(otp.getAttempts() + 1);
            passwordResetOtpRepository.save(otp);
            return false;
        }

        otp.setVerified(true);
        passwordResetOtpRepository.save(otp);
        return true;
    }

    /** Only succeeds after {@code verifyOtp} verified the latest code; consumes it. */
    @Transactional
    public boolean resetPassword(String email, String newPassword) {
        String normalized = normalizeEmail(email);

        PasswordResetOtp otp = passwordResetOtpRepository
                .findTopByEmailOrderByCreatedAtDesc(normalized)
                .orElse(null);

        if (otp == null || !otp.getVerified() || otp.getUsed()) {
            return false;
        }
        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        User user = userRepository.findByEmailIgnoreCase(normalized).orElse(null);
        if (user == null) {
            return false;
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        otp.setUsed(true);
        passwordResetOtpRepository.save(otp);
        return true;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private String generateNumericCode() {
        StringBuilder sb = new StringBuilder(otpLength);
        for (int i = 0; i < otpLength; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}
