package com.bgroceries.backend.social;

import com.bgroceries.backend.exception.UnauthorizedException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Jwk;
import io.jsonwebtoken.security.Jwks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GoogleSocialVerifierTest {

    private static final String CLIENT_ID = "test-client-id.apps.googleusercontent.com";
    private static final String JWKS_URL = "https://www.googleapis.com/oauth2/v3/certs";

    private MockRestServiceServer server;
    private GoogleSocialVerifier verifier;
    private KeyPair keyPair;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        keyPair = generator.generateKeyPair();

        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        verifier = new GoogleSocialVerifier(builder, CLIENT_ID);
    }

    @Test
    void validIdTokenReturnsProfile() {
        server.expect(requestTo(JWKS_URL))
                .andRespond(withSuccess(jwksJson(), MediaType.APPLICATION_JSON));

        SocialProfile profile = verifier.verify(signToken(b -> { }));

        assertThat(profile.provider()).isEqualTo("gmail");
        assertThat(profile.providerId()).isEqualTo("1234567890");
        assertThat(profile.email()).isEqualTo("user@gmail.com");
        assertThat(profile.fullName()).isEqualTo("Test User");
    }

    @Test
    void wrongAudienceIsRejected() {
        server.expect(requestTo(JWKS_URL))
                .andRespond(withSuccess(jwksJson(), MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> verifier.verify(signToken(b -> b.claim("aud", "another-client.apps.googleusercontent.com"))))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void wrongIssuerIsRejected() {
        server.expect(requestTo(JWKS_URL))
                .andRespond(withSuccess(jwksJson(), MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> verifier.verify(signToken(b -> b.issuer("https://evil.example.com"))))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void unverifiedEmailIsRejected() {
        server.expect(requestTo(JWKS_URL))
                .andRespond(withSuccess(jwksJson(), MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> verifier.verify(signToken(b -> b.claim("email_verified", false))))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void tokenSignedByUnknownKeyIsRejected() throws Exception {
        // A different key pair -> the kid lookup fails against the served JWKS.
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair otherPair = generator.generateKeyPair();

        server.expect(requestTo(JWKS_URL))
                .andRespond(withSuccess(jwksJson(), MediaType.APPLICATION_JSON));

        String token = Jwts.builder()
                .setHeaderParam("kid", "test-key")
                .issuer("https://accounts.google.com")
                .subject("1234567890")
                .claim("aud", CLIENT_ID)
                .claim("email", "user@gmail.com")
                .claim("email_verified", true)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(otherPair.getPrivate(), Jwts.SIG.RS256)
                .compact();

        assertThatThrownBy(() -> verifier.verify(token)).isInstanceOf(UnauthorizedException.class);
    }

    /** Builds a signed ID token with default valid claims, then applies the customizer. */
    private String signToken(Consumer<JwtBuilder> customizer) {
        JwtBuilder builder = Jwts.builder()
                .setHeaderParam("kid", "test-key")
                .issuer("https://accounts.google.com")
                .subject("1234567890")
                .claim("aud", CLIENT_ID)
                .claim("email", "user@gmail.com")
                .claim("email_verified", true)
                .claim("name", "Test User")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(keyPair.getPrivate(), Jwts.SIG.RS256);
        customizer.accept(builder);
        return builder.compact();
    }

    /** JWKS JSON with one key whose kid matches the tokens' header. */
    private String jwksJson() {
        Jwk<?> jwk = Jwks.builder().key((RSAPublicKey) keyPair.getPublic()).id("test-key").build();
        return "{\"keys\":[" + Jwks.UNSAFE_JSON(jwk) + "]}";
    }
}
