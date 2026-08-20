package com.bgroceries.backend.social;

import com.bgroceries.backend.exception.UnauthorizedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TelegramSocialVerifierTest {

    /** Bot token from the official Telegram "Checking authorization" docs example. */
    private static final String BOT_TOKEN = "135485712:AAEp9OM6hFrZ6HmmLNG7T2CAQbJqW3q7EmJ";

    /** Fixed clock so the auth_date freshness check is deterministic. */
    private static final long FIXED_NOW_SECONDS = 1515957914L; // auth_date + 60s

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TelegramSocialVerifier verifier =
            new TelegramSocialVerifier(objectMapper, BOT_TOKEN, () -> FIXED_NOW_SECONDS * 1000);

    /**
     * Known-answer vector: the docs example auth data + this bot token.
     * The expected hash was computed externally (not by the code under test):
     * 8af005321e64c8e37d6010400322fc615cead73a1abd3c238c93f802c14ddc0e
     */
    private static final String KNOWN_HASH = "8af005321e64c8e37d6010400322fc615cead73a1abd3c238c93f802c14ddc0e";

    @Test
    void validAuthObjectReturnsProfile() throws Exception {
        String json = authJson(Map.of(
                "auth_date", "1515957854",
                "first_name", "Brata",
                "id", "48264086",
                "last_name", "Rifki",
                "username", "bratarifki",
                "hash", KNOWN_HASH));

        SocialProfile profile = verifier.verify(json);

        assertThat(profile.provider()).isEqualTo("telegram");
        assertThat(profile.providerId()).isEqualTo("48264086");
        assertThat(profile.fullName()).isEqualTo("Brata Rifki");
        assertThat(profile.telegramUsername()).isEqualTo("bratarifki");
        assertThat(profile.email()).isNull();
    }

    @Test
    void tamperedHashIsRejected() throws Exception {
        String json = authJson(Map.of(
                "auth_date", "1515957854",
                "first_name", "Brata",
                "id", "48264086",
                "last_name", "Rifki",
                "username", "bratarifki",
                "hash", "0" + KNOWN_HASH.substring(1)));

        assertThatThrownBy(() -> verifier.verify(json)).isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void expiredAuthDateIsRejected() throws Exception {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("auth_date", String.valueOf(FIXED_NOW_SECONDS - 24 * 60 * 60 - 10));
        data.put("first_name", "Old");
        data.put("id", "999");
        data.put("hash", computeHash(data)); // correct hash, but too old

        assertThatThrownBy(() -> verifier.verify(authJson(data))).isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void futureAuthDateIsRejected() throws Exception {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("auth_date", String.valueOf(FIXED_NOW_SECONDS + 6 * 60));
        data.put("first_name", "Future");
        data.put("id", "1000");
        data.put("hash", computeHash(data));

        assertThatThrownBy(() -> verifier.verify(authJson(data))).isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void missingHashIsRejected() throws Exception {
        String json = authJson(Map.of(
                "auth_date", "1515957854",
                "first_name", "Brata",
                "id", "48264086"));

        assertThatThrownBy(() -> verifier.verify(json)).isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void missingIdIsRejected() throws Exception {
        String json = authJson(Map.of(
                "auth_date", "1515957854",
                "first_name", "Brata",
                "hash", KNOWN_HASH));

        assertThatThrownBy(() -> verifier.verify(json)).isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void malformedJsonIsRejected() {
        assertThatThrownBy(() -> verifier.verify("not-json")).isInstanceOf(UnauthorizedException.class);
    }

    private String authJson(Map<String, String> data) throws Exception {
        return objectMapper.writeValueAsString(data);
    }

    /** Test-side mirror of the Telegram algorithm, used only for fresh-timestamp cases. */
    private static String computeHash(Map<String, String> data) throws Exception {
        String checkString = data.entrySet().stream()
                .filter(e -> !"hash".equals(e.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("\n"));
        byte[] secretKey = MessageDigest.getInstance("SHA-256").digest(BOT_TOKEN.getBytes(StandardCharsets.UTF_8));
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secretKey, "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(checkString.getBytes(StandardCharsets.UTF_8)));
    }
}
