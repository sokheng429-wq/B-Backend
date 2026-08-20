package com.bgroceries.backend.social;

import com.bgroceries.backend.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Locator;
import io.jsonwebtoken.security.Jwk;
import io.jsonwebtoken.security.JwkSet;
import io.jsonwebtoken.security.Jwks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.security.Key;
import java.util.Set;

/**
 * Verifies a Google ID token (JWT, from Google Identity Services) against
 * Google's published JSON Web Key Set and returns the verified profile.
 *
 * <p>Account linking relies on the email claim, so this verifier requires
 * {@code email_verified=true} — only verified emails are trusted for linking.
 * The JWKS is fetched once and cached for an hour (Google rotates keys slowly).
 *
 * <p>Not configured (placeholder client id) → every call fails with 401.
 */
@Component
public class GoogleSocialVerifier implements SocialVerifier {

    private static final String JWKS_URL = "https://www.googleapis.com/oauth2/v3/certs";
    private static final Set<String> VALID_ISSUERS =
            Set.of("accounts.google.com", "https://accounts.google.com");
    private static final long CACHE_TTL_MS = 60 * 60 * 1000L; // 1 hour

    private final String clientId;
    private final RestClient restClient;

    private volatile JwkSet cachedJwkSet;
    private volatile long cacheLoadedAt;

    public GoogleSocialVerifier(RestClient.Builder restClientBuilder,
                                @Value("${app.social.google.client-id}") String clientId) {
        this.clientId = clientId;
        this.restClient = restClientBuilder.build();
    }

    @Override
    public String provider() {
        return "gmail";
    }

    @Override
    public SocialProfile verify(String token) {
        if (clientId.startsWith("your-")) {
            throw new UnauthorizedException("Invalid provider token");
        }
        try {
            Claims claims = Jwts.parser()
                    .keyLocator(keyLocator())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            validate(claims);

            return new SocialProfile(
                    provider(),
                    claims.getSubject(),
                    claims.get("email", String.class),
                    claims.get("name", String.class),
                    null,
                    claims.get("picture", String.class));
        } catch (JwtException | RestClientException | IllegalArgumentException e) {
            throw new UnauthorizedException("Invalid provider token");
        }
    }

    private void validate(Claims claims) {
        String issuer = claims.getIssuer();
        if (issuer == null || !VALID_ISSUERS.contains(issuer)) {
            throw new JwtException("Unexpected issuer");
        }
        Set<String> audience = claims.getAudience();
        if (audience == null || !audience.contains(clientId)) {
            throw new JwtException("Audience does not match client id");
        }
        String email = claims.get("email", String.class);
        Boolean emailVerified = claims.get("email_verified", Boolean.class);
        if (email == null || email.isBlank() || !Boolean.TRUE.equals(emailVerified)) {
            throw new JwtException("Email is required and must be verified");
        }
    }

    private Locator<Key> keyLocator() {
        JwkSet jwkSet = cachedJwkSet;
        long now = System.currentTimeMillis();
        if (jwkSet == null || now - cacheLoadedAt > CACHE_TTL_MS) {
            synchronized (this) {
                jwkSet = cachedJwkSet;
                if (jwkSet == null || System.currentTimeMillis() - cacheLoadedAt > CACHE_TTL_MS) {
                    jwkSet = fetchJwkSet();
                    cachedJwkSet = jwkSet;
                    cacheLoadedAt = System.currentTimeMillis();
                }
            }
        }
        final JwkSet set = jwkSet;
        return header -> {
            Object kid = header.get("kid");
            if (kid == null) {
                throw new JwtException("Token header has no kid");
            }
            for (Jwk<?> jwk : set.getKeys()) {
                if (kid.equals(jwk.getId())) {
                    return (Key) jwk.toKey();
                }
            }
            throw new JwtException("Unknown signing key id");
        };
    }

    private JwkSet fetchJwkSet() {
        String json = restClient.get().uri(JWKS_URL).retrieve().body(String.class);
        if (json == null || json.isBlank()) {
            throw new JwtException("Empty JWKS response");
        }
        return Jwks.setParser().build().parse(json);
    }
}
