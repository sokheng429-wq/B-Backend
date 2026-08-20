package com.bgroceries.backend.social;

import com.bgroceries.backend.exception.UnauthorizedException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Verifies a Facebook user access token (from the JS SDK) against the Graph API:
 * first {@code /debug_token} (using an app token {@code appId|appSecret}) to
 * confirm the token is valid and issued to our app, then fetches the public
 * profile {@code id/name/email}. Email is only present when the user granted
 * the {@code email} permission — linking falls back to {@code facebookId}.
 *
 * <p>Not configured (placeholder app id/secret) → every call fails with 401.
 */
@Component
public class FacebookSocialVerifier implements SocialVerifier {

    private static final String GRAPH_BASE_URL = "https://graph.facebook.com";

    private final String appId;
    private final String appSecret;
    private final String graphVersion;
    private final RestClient restClient;

    public FacebookSocialVerifier(RestClient.Builder restClientBuilder,
                                  @Value("${app.social.facebook.app-id}") String appId,
                                  @Value("${app.social.facebook.app-secret}") String appSecret,
                                  @Value("${app.social.facebook.graph-version}") String graphVersion) {
        this.appId = appId;
        this.appSecret = appSecret;
        this.graphVersion = graphVersion;
        this.restClient = restClientBuilder.build();
    }

    @Override
    public String provider() {
        return "facebook";
    }

    @Override
    public SocialProfile verify(String token) {
        if (appId.startsWith("your-") || appSecret.startsWith("your-")) {
            throw new UnauthorizedException("Invalid provider token");
        }
        try {
            String appToken = appId + "|" + appSecret;

            String debugUri = UriComponentsBuilder.fromHttpUrl(GRAPH_BASE_URL)
                    .pathSegment(graphVersion, "debug_token")
                    .queryParam("input_token", token)
                    .queryParam("access_token", appToken)
                    .build().encode().toUriString();

            DebugTokenResponse debug = restClient.get().uri(debugUri).retrieve().body(DebugTokenResponse.class);
            if (debug == null || debug.data() == null
                    || !Boolean.TRUE.equals(debug.data().isValid())
                    || !appId.equals(debug.data().appId())) {
                throw new UnauthorizedException("Invalid provider token");
            }

            String userId = debug.data().userId();
            String profileUri = UriComponentsBuilder.fromHttpUrl(GRAPH_BASE_URL)
                    .pathSegment(graphVersion, userId)
                    .queryParam("fields", "id,name,email")
                    .queryParam("access_token", token)
                    .build().encode().toUriString();

            FbProfile profile = restClient.get().uri(profileUri).retrieve().body(FbProfile.class);
            if (profile == null || profile.id() == null) {
                throw new UnauthorizedException("Invalid provider token");
            }

            return new SocialProfile(provider(), profile.id(), profile.email(), profile.name(), null, null);
        } catch (RestClientException e) {
            throw new UnauthorizedException("Invalid provider token");
        }
    }

    private record DebugTokenResponse(Data data) {
        private record Data(
                @JsonProperty("is_valid") Boolean isValid,
                @JsonProperty("user_id") String userId,
                @JsonProperty("app_id") String appId) {
        }
    }

    private record FbProfile(String id, String name, String email) {
    }
}
