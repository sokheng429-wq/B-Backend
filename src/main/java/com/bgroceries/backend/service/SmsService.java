package com.bgroceries.backend.service;

public interface SmsService {

    /**
     * Sends an OTP code by SMS to the given phone number.
     * Swap the implementation for a real Cambodian SMS gateway
     * (e.g. PlasGate, SMS Cambodia, Twilio) when going to production.
     */
    void sendOtp(String phoneNumber, String otp);
}
