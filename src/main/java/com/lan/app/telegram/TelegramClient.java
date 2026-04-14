package com.lan.app.telegram;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lan.app.config.TelegramConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class TelegramClient {

    private final TelegramConfig telegramConfig;
    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Inject
    public TelegramClient(
        TelegramConfig telegramConfig
    ) {
        this.telegramConfig = telegramConfig;
    }

    public void sendHtml(Long chatId, String text, Object replyMarkup) {
        try {
            Map<String, Object> body = new HashMap<>(); // ← HashMap вместо Map.of()
            body.put("chat_id", chatId);
            body.put("text", text);
            body.put("parse_mode", "HTML");
            if (replyMarkup != null) {
                body.put("reply_markup", replyMarkup); // ← добавляем только если не null
            }

            String url = telegramConfig.apiBaseUrl() + "/bot" + telegramConfig.botToken() + "/sendMessage";
            var req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();

            var resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 300) {
                throw new TelegramClientException("Telegram sendMessage failed: " + resp.body());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TelegramClientException("HTTP request was interrupted", e);
        } catch (Exception e) {
            throw new TelegramClientException("Failed to send message to Telegram", e);
        }
    }
}
