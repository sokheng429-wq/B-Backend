package com.bgroceries.backend.social;

import com.bgroceries.backend.exception.UnauthorizedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.function.LongSupplier;
import java.util.stream.Collectors;

/**
 * Verifies a Telegram Login Widget {@code auth} object using the official
 * algorithm (https://core.telegram.org/widgets/login-legacy "Checking
 * authorization"): the payload fields sorted by key and joined as
 * {@code key=value} lines are HMAC-SHA256 signed with {@code SHA256(botToken)}
 * as the key, and the lowercase hex digest must equal the {@code hash} field.
 * {@code auth_date} must be recent (not older than 24h, not far in the future).
 *
 * <p>Not configured (placeholder bot token) → every call fails with 401.
 */
@Component
public class TelegramSocialVerifier implements SocialVerifier {

    private static final long MAX_AGE_SECONDS = 24 * 60 * 60;
    private static final long FUTURE_SKEW_SECONDS = 5 * 60;

    private final String botToken;
    private final ObjectMapper objectMapper;
    private final LongSupplier nowMillis;

    @Autowired
    public TelegramSocialVerifier(ObjectMapper objectMapper,
                                  @Value("${app.social.telegram.bot-token}") String botToken) {
        this(objectMapper, botToken, System::currentTimeMillis);
    }

    /** Test constructor with an injectable clock. */
    TelegramSocialVerifier(ObjectMapper objectMapper, String botToken, LongSupplier nowMillis) {
        this.objectMapper = objectMapper;
        this.botToken = botToken;
        this.nowMillis = nowMillis;
    }

    @Override
    public String provider() {
        return "telegram";
    }

    @Override
    public SocialProfile verify(String token) {
        if (botToken.startsWith("your-")) {
            throw new UnauthorizedException("Invalid provider token");
        }
        Map<String, String> data;
        try {
            data = objectMapper.readValue(token, new TypeReference<Map<String, String>>() { });
        } catch (JsonProcessingException e) {
            throw new UnauthorizedException("Invalid provider token");
        }

        String hash = data.get("hash");
        String authDateStr = data.get("auth_date");
        if (hash == null || hash.isEmpty() || authDateStr == null || data.get("id") == null) {
            throw new UnauthorizedException("Invalid provider token");
        }

        long authDate;
        try {
            authDate = Long.parseLong(authDateStr);
        } catch (NumberFormatException e) {
            throw new UnauthorizedException("Invalid provider token");
        }
        long now = nowMillis.getAsLong() / 1000;
        if (authDate < now - MAX_AGE_SECONDS || authDate > now + FUTURE_SKEW_SECONDS) {
            throw new UnauthorizedException("Invalid provider token");
        }

        String computed = computeHash(data);
        byte[] expected = hash.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(computed.getBytes(StandardCharsets.UTF_8), expected)) {
            throw new UnauthorizedException("Invalid provider token");
        }

        return new SocialProfile(
                provider(),
                data.get("id"),
                null,
                fullName(data.get("first_name"), data.get("last_name")),
                data.get("username"),
                data.get("photo_url"));
    }

    private String computeHash(Map<String, String> data) {
        String checkString = data.entrySet().stream()
                .filter(entry -> !"hash".equals(entry.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("\n"));

        try {
            byte[] secretKey = MessageDigest.getInstance("SHA-256")
                    .digest(botToken.getBytes(StandardCharsets.UTF_8));
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey, "HmacSHA256"));
            byte[] hmac = mac.doFinal(checkString.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Telegram verification unavailable", e);
        }
    }

    private String fullName(String firstName, String lastName) {
        if (firstName == null) {
            return lastName;
        }
        return lastName == null ? firstName : firstName + " " + lastName;
    }
}
