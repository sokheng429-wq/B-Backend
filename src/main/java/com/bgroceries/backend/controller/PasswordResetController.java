package com.bgroceries.backend.controller;

import com.bgroceries.backend.service.OtpService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Email-based forgot-password flow (coexists with the phone-based one in
 * {@link AuthController}). OTP codes are delivered via {@code EmailService}
 * (console log until a real gateway is wired) and read from the backend log.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final OtpService otpService;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        // Returns the raw code in dev (app.otp.expose-in-response=true) so the frontend
        // can be tested without a real email provider — same pattern as the SMS OTP flow.
        String debugOtp = otpService.generateAndSendOtp(request.getEmail());
        return ResponseEntity.ok(new MessageResponse(
            "An OTP has been sent to your email address.", debugOtp,
            otpService.getExpiryMinutes() * 60
        ));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyEmailOtpRequest request) {
        boolean valid = otpService.verifyOtp(request.getEmail(), request.getOtp());
        if (!valid) {
            return ResponseEntity.badRequest()
                .body(new MessageResponse("Invalid or expired OTP.", null, null));
        }
        return ResponseEntity.ok(new MessageResponse("OTP verified successfully.", null, null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordBody request) {
        boolean success = otpService.resetPassword(request.getEmail(), request.getNewPassword());
        if (!success) {
            return ResponseEntity.badRequest()
                .body(new MessageResponse("Unable to reset password. Please restart the process.", null, null));
        }
        return ResponseEntity.ok(new MessageResponse("Password reset successfully.", null, null));
    }

    @Data
    static class ForgotPasswordRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email address")
        private String email;
    }

    @Data
    static class VerifyEmailOtpRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email address")
        private String email;

        @NotBlank(message = "OTP is required")
        @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be 6 digits")
        private String otp;
    }

    @Data
    static class ResetPasswordBody {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email address")
        private String email;

        @NotBlank(message = "New password is required")
        @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
        private String newPassword;
    }

    @Data
    static class MessageResponse {
        private final String message;
        /** Non-null only in dev (app.otp.expose-in-response=true). */
        private final String debugOtp;
        /** How long the code stays valid (server-configured). Null on verify/reset responses. */
        private final Integer expiresInSeconds;
    }
}
