package com.bgroceries.backend.social;

import com.bgroceries.backend.exception.UnauthorizedException;

/**
 * Verifies a provider-issued credential and returns the verified profile.
 * Implementations must be {@code @Component}s; all of them are injected as a
 * {@code List<SocialVerifier>} into {@code AuthService} and selected by
 * {@link #provider()}.
 *
 * <p>Every verification failure (malformed, expired, untrusted, or generated
 * with unconfigured placeholder credentials) must throw an opaque
 * {@link UnauthorizedException}.
 */
public interface SocialVerifier {

    /** Provider name as used in the API contract: {@code gmail | telegram | facebook}. */
    String provider();

    /**
     * Verifies the raw credential and returns the linked profile.
     *
     * @param token Google ID token, Facebook access token, or Telegram Login
     *              Widget auth object (as JSON)
     * @throws UnauthorizedException if the credential cannot be trusted
     */
    SocialProfile verify(String token);
}
