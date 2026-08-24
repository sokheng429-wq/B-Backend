package com.bgroceries.backend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks the last-activity timestamp for every active JWT access token.
 * <p>
 * The map key is the raw JWT string; the value is the {@link Instant} of the
 * most recent authenticated request that carried that token.
 * <p>
 * A scheduled job runs every minute to evict entries whose last-activity is
 * older than the configured inactivity timeout so that the map does not grow
 * without bound (e.g. if the client never calls the logout endpoint).
 */
@Component
public class TokenActivityStore {

    /** Map<rawJwt, lastActivityInstant> */
    private final ConcurrentHashMap<String, Instant> store = new ConcurrentHashMap<>();

    @Value("${app.jwt.inactivity-timeout-ms:300000}")
    private long inactivityTimeoutMs;

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    /**
     * Register a token when it is first issued (login / register / social login).
     * Records {@code now} as the initial activity timestamp.
     */
    public void register(String token) {
        store.put(token, Instant.now());
    }

    /**
     * Returns {@code true} when the token is known AND the time elapsed since
     * its last recorded activity does NOT exceed the inactivity timeout.
     */
    public boolean isActive(String token) {
        Instant last = store.get(token);
        if (last == null) {
            return false;
        }
        long elapsedMs = Instant.now().toEpochMilli() - last.toEpochMilli();
        return elapsedMs <= inactivityTimeoutMs;
    }

    /**
     * Touch a token — updates its last-activity timestamp to {@code now}.
     * Call this every time an authenticated request successfully passes through
     * {@link JwtAuthFilter}.
     */
    public void touch(String token) {
        // Only update if the token is still tracked; avoids re-adding evicted tokens.
        store.computeIfPresent(token, (k, v) -> Instant.now());
    }

    /**
     * Remove a token from the store (explicit logout or inactivity eviction).
     */
    public void evict(String token) {
        store.remove(token);
    }

    // ------------------------------------------------------------------
    // Background cleanup — runs every 60 seconds
    // ------------------------------------------------------------------

    /**
     * Periodically removes entries that have been inactive longer than the
     * configured timeout so that the map size stays bounded.
     */
    @Scheduled(fixedDelay = 60_000)
    public void evictExpiredEntries() {
        long now = Instant.now().toEpochMilli();
        store.entrySet().removeIf(entry ->
                now - entry.getValue().toEpochMilli() > inactivityTimeoutMs
        );
    }
}
