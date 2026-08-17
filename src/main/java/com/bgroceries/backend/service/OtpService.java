package com.bgroceries.backend.service;

import com.bgroceries.backend.entity.OtpCode;
import com.bgroceries.backend.enums.OtpPurpose;
import com.bgroceries.backend.exception.BadRequestException;
import com.bgroceries.backend.repository.OtpCodeRepository;
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
    private final PasswordEncoder passwordEncoder;
    private final SmsService smsService;

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

    private String generateNumericCode() {
        StringBuilder sb = new StringBuilder(otpLength);
        for (int i = 0; i < otpLength; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}
