package com.bgroceries.backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Diagnostic endpoint to test OAuth2 provider connectivity.
 * REMOVE IN PRODUCTION!
 */
@Slf4j
@RestController
@RequestMapping("/api/oauth2/diagnostic")
public class OAuth2DiagnosticController {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${spring.security.oauth2.client.registration.facebook.client-id}")
    private String facebookClientId;

    @Value("${spring.security.oauth2.client.registration.facebook.client-secret}")
    private String facebookClientSecret;

    @GetMapping("/credentials")
    public Map<String, Object> checkCredentials() {
        Map<String, Object> result = new HashMap<>();

        // Google credentials check
        Map<String, Object> google = new HashMap<>();
        google.put("clientIdConfigured", googleClientId != null && !googleClientId.isBlank());
        google.put("clientSecretConfigured", googleClientSecret != null && !googleClientSecret.isBlank());
        google.put("clientIdLength", googleClientId != null ? googleClientId.length() : 0);
        google.put("clientSecretLength", googleClientSecret != null ? googleClientSecret.length() : 0);
        result.put("google", google);

        // Facebook credentials check
        Map<String, Object> facebook = new HashMap<>();
        facebook.put("clientIdConfigured", facebookClientId != null && !facebookClientId.isBlank());
        facebook.put("clientSecretConfigured", facebookClientSecret != null && !facebookClientSecret.isBlank());
        facebook.put("clientIdLength", facebookClientId != null ? facebookClientId.length() : 0);
        facebook.put("clientSecretLength", facebookClientSecret != null ? facebookClientSecret.length() : 0);
        result.put("facebook", facebook);

        return result;
    }

    @GetMapping("/test-google")
    public Map<String, Object> testGoogleConnectivity() {
        Map<String, Object> result = new HashMap<>();
        try {
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(
                "https://accounts.google.com/.well-known/openid-configuration",
                String.class
            );
            result.put("status", "success");
            result.put("message", "Can reach Google OAuth2 endpoints");
            result.put("reachable", true);
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "Cannot reach Google OAuth2 endpoints: " + e.getMessage());
            result.put("reachable", false);
            log.error("Failed to reach Google OAuth2 endpoints", e);
        }
        return result;
    }

    @GetMapping("/test-facebook")
    public Map<String, Object> testFacebookConnectivity() {
        Map<String, Object> result = new HashMap<>();
        try {
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(
                "https://graph.facebook.com/v21.0/?access_token=" + facebookClientId + "|" + facebookClientSecret,
                String.class
            );
            result.put("status", "success");
            result.put("message", "Can reach Facebook Graph API");
            result.put("reachable", true);
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "Cannot reach Facebook Graph API: " + e.getMessage());
            result.put("reachable", false);
            log.error("Failed to reach Facebook Graph API", e);
        }
        return result;
    }
}
