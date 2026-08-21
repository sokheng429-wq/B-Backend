package com.bgroceries.backend.service.impl;

import com.bgroceries.backend.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Sends the forgot-password OTP by email over SMTP (Gmail SMTP works locally).
 * When no SMTP credentials are configured — or sending fails — falls back to
 * logging the code to the console so the dev flow is never blocked (same
 * pattern as {@link ConsoleSmsServiceImpl}).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmtpEmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String smtpUsername;

    @Override
    public void sendOtp(String email, String otp) {
        if (smtpUsername == null || smtpUsername.isBlank()) {
            log.info("=== [EMAIL SIMULATION] OTP {} sent to {} (SMTP not configured) ===", otp, email);
            return;
        }

        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, false, "UTF-8");
            helper.setTo(email);
            helper.setSubject("B'Groceries — password reset code");
            helper.setText(
                    "Your B'Groceries password reset code is: " + otp
                            + "\n\nIt expires in 5 minutes. If you didn't request this, you can ignore this email.",
                    false);
            mailSender.send(mime);
            log.info("Sent OTP email to {}", email);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", email, e.getMessage());
            log.info("=== [EMAIL SIMULATION] OTP {} sent to {} (SMTP send failed) ===", otp, email);
        }
    }
}
