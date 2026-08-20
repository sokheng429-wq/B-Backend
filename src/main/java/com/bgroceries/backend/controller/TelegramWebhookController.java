package com.bgroceries.backend.controller;

import com.bgroceries.backend.service.TelegramService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/telegram/webhook")
@RequiredArgsConstructor
@Slf4j
public class TelegramWebhookController {

    private final TelegramService telegramService;

    @Value("${telegram.webhook.secret:}")
    private String webhookSecret;

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestHeader(value = "X-Telegram-Bot-Api-Secret-Token", required = false) String secretToken,
            @RequestBody JsonNode update) {

        if (webhookSecret != null && !webhookSecret.isEmpty() && !webhookSecret.equals(secretToken)) {
            log.warn("Invalid webhook secret token");
            return ResponseEntity.status(403).body("Forbidden");
        }

        try {
            if (update.has("message")) {
                JsonNode message = update.get("message");
                JsonNode from = message.get("from");

                Long telegramUserId = from.get("id").asLong();
                String telegramUsername = from.has("username") ? from.get("username").asText() : null;
                String telegramFirstName = from.has("first_name") ? from.get("first_name").asText() : null;

                if (message.has("text")) {
                    String text = message.get("text").asText();

                    if (text.startsWith("/start ")) {
                        String sessionToken = text.substring(7).trim();
                        telegramService.processStartCommand(telegramUserId, telegramUsername, telegramFirstName, sessionToken);
                        log.info("Processed /start command for user {} with session {}", telegramUserId, sessionToken);
                    }
                }
            }

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.ok("OK");
        }
    }
}
