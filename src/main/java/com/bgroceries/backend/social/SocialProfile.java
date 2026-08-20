package com.bgroceries.backend.social;

/**
 * A provider-verified identity returned by a {@link SocialVerifier}.
 * {@code email} is only set when the provider has verified it
 * (Google requires {@code email_verified=true}; Facebook only when the
 * {@code email} permission was granted; Telegram never sends an email).
 */
public record SocialProfile(
        String provider,          // gmail | telegram | facebook (matches the API contract)
        String providerId,        // stable account id at the provider
        String email,             // nullable
        String fullName,          // nullable
        String telegramUsername,  // telegram handle without @, nullable
        String photoUrl) {        // nullable
}
