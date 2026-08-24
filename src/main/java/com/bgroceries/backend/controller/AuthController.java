package com.bgroceries.backend.controller;

import com.bgroceries.backend.dto.request.LoginRequest;
import com.bgroceries.backend.dto.request.PhoneRequest;
import com.bgroceries.backend.dto.request.RegisterRequest;
import com.bgroceries.backend.dto.request.ResetPasswordRequest;
import com.bgroceries.backend.dto.request.SocialLoginRequest;
import com.bgroceries.backend.dto.request.VerifyOtpRequest;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.dto.response.AuthResponse;
import com.bgroceries.backend.dto.response.OtpSentResponse;
import com.bgroceries.backend.dto.response.ResetTokenResponse;
import com.bgroceries.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ---- Register ----
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully", response));
    }

    // ---- Login with password ----
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    // ---- Social login (Gmail / Telegram / Facebook) ----
    @PostMapping("/social")
    public ResponseEntity<ApiResponse<AuthResponse>> socialLogin(@Valid @RequestBody SocialLoginRequest request) {
        AuthResponse response = authService.socialLogin(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    // ---- Login with OTP ----
    @PostMapping("/login/otp/send")
    public ResponseEntity<ApiResponse<OtpSentResponse>> sendLoginOtp(@Valid @RequestBody PhoneRequest request) {
        OtpSentResponse response = authService.sendLoginOtp(request);
        return ResponseEntity.ok(ApiResponse.success("OTP sent successfully", response));
    }

    @PostMapping("/login/otp/verify")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyLoginOtp(@Valid @RequestBody VerifyOtpRequest request) {
        AuthResponse response = authService.verifyLoginOtp(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    // ---- Forgot password (OTP by phone) ----
    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<ApiResponse<OtpSentResponse>> sendForgotPasswordOtp(@Valid @RequestBody PhoneRequest request) {
        OtpSentResponse response = authService.sendForgotPasswordOtp(request);
        return ResponseEntity.ok(ApiResponse.success("OTP sent successfully", response));
    }

    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<ApiResponse<ResetTokenResponse>> verifyForgotPasswordOtp(@Valid @RequestBody VerifyOtpRequest request) {
        ResetTokenResponse response = authService.verifyForgotPasswordOtp(request);
        return ResponseEntity.ok(ApiResponse.success("OTP verified successfully", response));
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully"));
    }

    // ---- Logout ----
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        authService.logout(authorizationHeader);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }
}
