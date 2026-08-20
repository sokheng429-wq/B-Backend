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
public class SocialLoginRequest {

    /** One of: gmail | telegram | facebook */
    @NotBlank(message = "Provider is required")
    private String provider;

    /**
     * Optional. When blank, a stable demo account per provider is used
     * (one-click social login without an identity prompt).
     */
    private String identifier;

    /**
     * Optional. The provider-issued credential for REAL social login, verified
     * server-side. Format depends on the provider:
     * <ul>
     *   <li>{@code gmail}: Google ID token (JWT from Google Identity Services)</li>
     *   <li>{@code facebook}: Facebook user access token (from the JS SDK)</li>
     *   <li>{@code telegram}: JSON string of the Login Widget {@code auth} object
     *       ({@code id, first_name, last_name, username, photo_url, auth_date, hash})</li>
     * </ul>
     * When present it takes precedence over {@code identifier}. When absent, the
     * legacy demo-account behavior applies.
     */
    private String token;
}
