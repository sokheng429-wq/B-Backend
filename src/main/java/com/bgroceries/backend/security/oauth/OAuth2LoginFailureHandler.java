package com.bgroceries.backend.security.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class OAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                         AuthenticationException exception) throws IOException, ServletException {

        // Log the full error details for debugging
        log.error("OAuth2 Login Failed: {}", exception.getMessage(), exception);
        log.error("Exception Type: {}", exception.getClass().getName());
        if (exception.getCause() != null) {
            log.error("Root Cause: {}", exception.getCause().getMessage(), exception.getCause());
        }

        String message = exception.getMessage() != null ? exception.getMessage() : "Social login failed";

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("error", URLEncoder.encode(message, StandardCharsets.UTF_8))
                .build().toUriString();

        log.info("Redirecting to frontend with error: {}", targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
