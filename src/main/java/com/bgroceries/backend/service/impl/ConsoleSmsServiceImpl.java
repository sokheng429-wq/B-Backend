package com.bgroceries.backend.service.impl;

import com.bgroceries.backend.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Temporary SMS implementation that just logs the OTP to the console.
 * This lets you build and test the full Register / Login / OTP / Forgot-password
 * flow right now without needing a real SMS gateway account yet.
 *
 * TODO: Replace this with a real integration before going to production, e.g.:
 * - Twilio (https://www.twilio.com)
 * - PlasGate (Cambodia)
 * - SMS Cambodia / other local aggregators
 *
 * Just implement SmsService with the provider's SDK/HTTP API and it will be
 * picked up automatically (only one SmsService bean should exist).
 */
@Slf4j
@Service
public class ConsoleSmsServiceImpl implements SmsService {

    @Override
    public void sendOtp(String phoneNumber, String otp) {
        log.info("=== [SMS SIMULATION] OTP {} sent to {} ===", otp, phoneNumber);
    }
}
