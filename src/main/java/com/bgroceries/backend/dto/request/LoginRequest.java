package com.bgroceries.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /** Username, full name, email (Gmail), Telegram or Facebook handle. */
    @NotBlank(message = "Username, email, Telegram or Facebook is required")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;
}
