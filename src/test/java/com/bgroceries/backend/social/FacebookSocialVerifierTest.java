package com.bgroceries.backend.social;

import com.bgroceries.backend.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class FacebookSocialVerifierTest {

    private static final String APP_ID = "1234567890";
    private static final String APP_SECRET = "test-app-secret";
    private static final String VERSION = "v21.0";

    private MockRestServiceServer server;
    private FacebookSocialVerifier verifier;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        verifier = new FacebookSocialVerifier(builder, APP_ID, APP_SECRET, VERSION);
    }

    @Test
    void validAccessTokenReturnsProfile() {
        server.expect(path("/v21.0/debug_token"))
                .andRespond(withSuccess(debugJson(true, "12345", APP_ID), MediaType.APPLICATION_JSON));
        server.expect(path("/v21.0/12345"))
                .andRespond(withSuccess("{\"id\":\"12345\",\"name\":\"Test User\",\"email\":\"fb@test.com\"}",
                        MediaType.APPLICATION_JSON));

        SocialProfile profile = verifier.verify("user-access-token");

        assertThat(profile.provider()).isEqualTo("facebook");
        assertThat(profile.providerId()).isEqualTo("12345");
        assertThat(profile.email()).isEqualTo("fb@test.com");
        assertThat(profile.fullName()).isEqualTo("Test User");
    }

    @Test
    void invalidTokenIsRejected() {
        server.expect(path("/v21.0/debug_token"))
                .andRespond(withSuccess(debugJson(false, "12345", APP_ID), MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> verifier.verify("bad-token")).isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void tokenFromAnotherAppIsRejected() {
        server.expect(path("/v21.0/debug_token"))
                .andRespond(withSuccess(debugJson(true, "12345", "9999999999"), MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> verifier.verify("token")).isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void profileWithoutEmailIsAccepted() {
        server.expect(path("/v21.0/debug_token"))
                .andRespond(withSuccess(debugJson(true, "12345", APP_ID), MediaType.APPLICATION_JSON));
        server.expect(path("/v21.0/12345"))
                .andRespond(withSuccess("{\"id\":\"12345\",\"name\":\"No Email User\"}",
                        MediaType.APPLICATION_JSON));

        SocialProfile profile = verifier.verify("token");

        assertThat(profile.email()).isNull();
        assertThat(profile.providerId()).isEqualTo("12345");
    }

    @Test
    void graphErrorResponseIsRejected() {
        server.expect(path("/v21.0/debug_token"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .body("{\"error\":{\"message\":\"Invalid OAuth access token\",\"code\":190}}"));

        assertThatThrownBy(() -> verifier.verify("token")).isInstanceOf(UnauthorizedException.class);
    }

    private static RequestMatcher path(String expectedPath) {
        return request -> request.getURI().getPath().equals(expectedPath);
    }

    private static String debugJson(boolean valid, String userId, String appId) {
        return "{\"data\":{\"is_valid\":" + valid + ",\"user_id\":\"" + userId + "\",\"app_id\":\"" + appId + "\"}}";
    }
}
