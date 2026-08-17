package com.bgroceries.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpSentResponse {
    private String phoneNumber;
    private Integer expiresInSeconds;

    /**
     * Only populated when app.otp.expose-in-response=true (local/dev testing
     * without a real SMS gateway wired up yet). Must be false/omitted in production.
     */
    private String debugOtp;
}
