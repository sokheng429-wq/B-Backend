package com.bgroceries.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper endpoint for debugging OAuth2 configuration.
 * REMOVE THIS IN PRODUCTION - only for development!
 */
@RestController
@RequestMapping("/api/oauth2")
public class OAuth2TestController {

    @Value("${app.oauth2.redirect-uri}")
    private String frontendRedirectUri;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.facebook.client-id}")
    private String facebookClientId;

    @GetMapping("/config")
    public Map<String, Object> getOAuth2Config() {
        Map<String, Object> config = new HashMap<>();

        Map<String, String> google = new HashMap<>();
        google.put("clientId", googleClientId);
        google.put("authorizationUrl", "http://localhost:8081/oauth2/authorization/google");
        google.put("callbackUrl", "http://localhost:8081/login/oauth2/code/google");
        google.put("frontendRedirectUrl", frontendRedirectUri);

        Map<String, String> facebook = new HashMap<>();
        facebook.put("clientId", facebookClientId);
        facebook.put("authorizationUrl", "http://localhost:8081/oauth2/authorization/facebook");
        facebook.put("callbackUrl", "http://localhost:8081/login/oauth2/code/facebook");
        facebook.put("frontendRedirectUrl", frontendRedirectUri);

        config.put("google", google);
        config.put("facebook", facebook);

        Map<String, String> instructions = new HashMap<>();
        instructions.put("step1", "Configure redirect URIs in Google/Facebook console");
        instructions.put("step2", "Use authorizationUrl in your frontend OAuth buttons");
        instructions.put("step3", "After success, user will be redirected to frontendRedirectUrl with token");
        config.put("instructions", instructions);

        return config;
    }
}
