package com.bgroceries.backend.security.oauth;

import com.bgroceries.backend.entity.User;
import com.bgroceries.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * Runs after Google/Facebook redirect back to us with the user's profile.
 * Finds the matching local User (by provider + providerId), creating one on
 * first login, then hands Spring Security an OAuth2User wrapping it so the
 * rest of the OAuth2 login flow (and our success handler) can read it.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            log.info("=== OAuth2 User Load Started ===");
            log.info("Provider: {}", userRequest.getClientRegistration().getRegistrationId());

            OAuth2User oAuth2User = super.loadUser(userRequest);

            String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "google" | "facebook"
            Map<String, Object> attributes = oAuth2User.getAttributes();

            log.info("Received OAuth2 attributes: {}", attributes.keySet());

            String provider = registrationId.toLowerCase();
            String providerId = extractProviderId(attributes, provider);
            String email = (String) attributes.get("email");
            String name = (String) attributes.get("name");
            String avatarUrl = extractAvatarUrl(provider, attributes);

            log.info("Extracted - Provider: {}, ProviderId: {}, Email: {}, Name: {}",
                    provider, providerId, email, name);

            User user = findOrCreateUser(provider, providerId, email, name, avatarUrl);

            log.info("User loaded successfully: {} (ID: {})", user.getUsername(), user.getId());
            log.info("=== OAuth2 User Load Completed ===");

            return new CustomOAuth2User(user, attributes);
        } catch (Exception e) {
            log.error("=== OAuth2 User Load Failed ===");
            log.error("Error loading OAuth2 user: {}", e.getMessage(), e);
            throw e;
        }
    }

    private String extractProviderId(Map<String, Object> attributes, String provider) {
        if ("google".equals(provider)) {
            return String.valueOf(attributes.get("sub")); // Google uses "sub" for user ID
        } else if ("facebook".equals(provider)) {
            return String.valueOf(attributes.get("id")); // Facebook uses "id"
        }
        throw new IllegalArgumentException("Unsupported provider: " + provider);
    }

    private User findOrCreateUser(String provider, String providerId, String email, String name, String avatarUrl) {
        // Try to find by provider ID columns
        User user = null;
        if ("google".equals(provider)) {
            user = userRepository.findByGoogleId(providerId).orElse(null);
        } else if ("facebook".equals(provider)) {
            user = userRepository.findByFacebookId(providerId).orElse(null);
        }

        // If not found and email exists, link to existing account by email
        if (user == null && email != null && !email.isBlank()) {
            user = userRepository.findByEmailIgnoreCase(email).orElse(null);
            if (user != null) {
                // Link this provider ID to the existing user
                stampProviderId(user, provider, providerId);
                user.setLoginProvider(provider); // Track which provider was used for this login
                userRepository.save(user);
                return user;
            }
        }

        // Create new user if not found
        if (user == null) {
            user = createUser(provider, providerId, email, name, avatarUrl);
        } else {
            // Update login provider for existing user
            user.setLoginProvider(provider);
            userRepository.save(user);
        }

        return user;
    }

    private void stampProviderId(User user, String provider, String providerId) {
        if ("google".equals(provider)) {
            user.setGoogleId(providerId);
        } else if ("facebook".equals(provider)) {
            user.setFacebookId(providerId);
        }
    }

    private User createUser(String provider, String providerId, String email, String name, String avatarUrl) {
        String username = resolveUsername(name, provider, providerId);
        String resolvedEmail = resolveEmail(email, provider, providerId);

        User user = User.builder()
                .username(username)
                .fullName(name != null ? name : username)
                .email(resolvedEmail)
                .googleId("google".equals(provider) ? providerId : null)
                .facebookId("facebook".equals(provider) ? providerId : null)
                .loginProvider(provider)  // Track which provider was used for this login
                .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role("USER")
                .enabled(true)
                .build();

        return userRepository.save(user);
    }

    private String resolveEmail(String email, String provider, String providerId) {
        if (email != null && !email.isBlank()) {
            // Check if email is already taken
            if (!userRepository.existsByEmail(email)) {
                return email;
            }
        }
        // Generate a unique placeholder email
        return provider + "_" + providerId + "@bgroceries.social";
    }

    private String resolveUsername(String name, String provider, String providerId) {
        String base = (name != null && !name.isBlank())
                ? name.trim().replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9._-]", "")
                : provider + "_" + providerId;

        String candidate = base.toLowerCase();
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base.toLowerCase() + suffix++;
        }
        return candidate;
    }

    private String extractAvatarUrl(String provider, Map<String, Object> attributes) {
        if ("google".equals(provider)) {
            Object picture = attributes.get("picture");
            return picture != null ? picture.toString() : null;
        }

        // Facebook nests the photo under picture.data.url
        if ("facebook".equals(provider)) {
            Object pictureObj = attributes.get("picture");
            if (pictureObj instanceof Map<?, ?> pictureMap) {
                Object data = pictureMap.get("data");
                if (data instanceof Map<?, ?> dataMap) {
                    Object url = dataMap.get("url");
                    return url != null ? url.toString() : null;
                }
            }
        }
        return null;
    }
}
