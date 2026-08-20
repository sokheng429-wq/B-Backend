package com.bgroceries.backend.security.oauth;

import com.bgroceries.backend.security.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * Fires once Spring Security has finished the Google/Facebook OAuth2 dance
 * and has a CustomOAuth2User in hand. We don't keep server-side sessions
 * (the app is stateless JWT), so instead of leaving the person logged in via
 * a cookie we mint our own JWT and hand it back to the SPA as a query param
 * on a redirect - the frontend's OAuth2Redirect page picks it up from there.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException, ServletException {

        try {
            log.info("=== OAuth2 Login Success Handler Started ===");
            log.info("Authentication type: {}", authentication.getClass().getName());
            log.info("Principal type: {}", authentication.getPrincipal().getClass().getName());

            CustomOAuth2User principal = (CustomOAuth2User) authentication.getPrincipal();
            log.info("User ID: {}, Username: {}, Role: {}",
                    principal.getUser().getId(),
                    principal.getUser().getUsername(),
                    principal.getUser().getRole());

            String token = jwtUtil.generateAccessToken(
                    principal.getUser().getUsername(),
                    principal.getUser().getId(),
                    principal.getUser().getRole()
            );

            log.info("✅ JWT token generated successfully (length: {})", token.length());

            String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("token", token)
                    .build().toUriString();

            log.info("🔄 Redirecting to: {}", targetUrl);
            log.info("=== OAuth2 Login Success Handler Completed ===");

            clearAuthenticationAttributes(request);
            getRedirectStrategy().sendRedirect(request, response, targetUrl);

        } catch (Exception e) {
            log.error("=== OAuth2 Login Success Handler FAILED ===");
            log.error("Error during OAuth2 success handling: {}", e.getMessage(), e);

            // Redirect to frontend with error
            String errorUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("error", "Failed to generate authentication token")
                    .build().toUriString();

            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }
}
