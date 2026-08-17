package com.bgroceries.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Used for endpoints that only require a phone number:
 * login-with-OTP "send code" step and forgot-password "send code" step.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhoneRequest {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(0|\\+855)[0-9]{8,9}$", message = "Invalid Cambodian phone number")
    private String phoneNumber;
}
