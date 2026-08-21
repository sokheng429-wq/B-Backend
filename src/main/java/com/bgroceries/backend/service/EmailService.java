package com.bgroceries.backend.service;

public interface EmailService {

    /**
     * Sends an OTP code by email to the given address.
     * Swap the implementation for a real email provider (SendGrid, Resend, SMTP, ...)
     * when going to production. Only one {@code EmailService} bean may exist.
     */
    void sendOtp(String email, String otp);
}
