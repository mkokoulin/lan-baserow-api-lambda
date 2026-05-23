package com.lan.app.service;

import com.lan.app.repository.PaymentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

@ApplicationScoped
public class PaymentService {

    private static final Logger log = Logger.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepo;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @ConfigProperty(name = "app.telegram-bot-token", defaultValue = "")
    String botToken;

    @ConfigProperty(name = "app.telegram-admin-chat-id", defaultValue = "")
    String adminChatId;

    public PaymentService(PaymentRepository paymentRepo) {
        this.paymentRepo = paymentRepo;
    }

    public UUID createPayment(String registrationId, String eventName, String guestName, String phone,
                              BigDecimal amount, byte[] fileBytes, String filename) {
        var result = paymentRepo.create(registrationId, eventName, guestName, phone,
                amount, fileBytes, filename);

        notifyAdmin(result.id(), eventName, guestName, phone, amount, result.proofUrl());
        return result.id();
    }

    public PaymentRepository.ApproveResult approve(UUID paymentId) {
        return paymentRepo.approve(paymentId);
    }

    public PaymentRepository.RejectResult reject(UUID paymentId) {
        return paymentRepo.reject(paymentId);
    }

    private void notifyAdmin(UUID paymentId, String eventName, String guestName, String phone,
                              BigDecimal amount, String proofUrl) {
        if (botToken.isBlank() || adminChatId.isBlank()) {
            log.warn("Telegram credentials not configured, skipping admin notification");
            return;
        }
        try {
            String caption = buildCaption(eventName, guestName, phone, amount, proofUrl, paymentId);
            String replyMarkup = buildReplyMarkup(paymentId);
            String jsonBody = "{\"chat_id\":" + adminChatId
                + ",\"text\":" + jsonString(caption)
                + ",\"parse_mode\":\"HTML\""
                + ",\"reply_markup\":" + replyMarkup
                + ",\"disable_web_page_preview\":false}";

            var req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.telegram.org/bot" + botToken + "/sendMessage"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

            var resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                log.warnf("Telegram sendMessage failed %d: %s", resp.statusCode(), resp.body());
            }
        } catch (Exception e) {
            log.warnf("Failed to send admin Telegram notification for payment=%s: %s", paymentId, e.getMessage());
        }
    }

    private String buildCaption(String eventName, String guestName, String phone,
                                 BigDecimal amount, String proofUrl, UUID paymentId) {
        StringBuilder sb = new StringBuilder();
        sb.append("💳 <b>Подтверждение оплаты</b>\n\n");
        if (eventName != null && !eventName.isBlank()) sb.append("🎪 ").append(eventName).append("\n");
        if (guestName != null && !guestName.isBlank())  sb.append("👤 ").append(guestName).append("\n");
        if (phone != null && !phone.isBlank())          sb.append("📱 ").append(phone).append("\n");
        if (amount != null)                             sb.append("💰 ").append(amount.toPlainString()).append(" AMD\n");
        if (proofUrl != null && !proofUrl.isBlank())    sb.append("\n<a href=\"").append(proofUrl).append("\">📎 Просмотреть скриншот</a>\n");
        sb.append("\n🆔 ").append(paymentId);
        return sb.toString();
    }

    private String buildReplyMarkup(UUID paymentId) {
        return "{\"inline_keyboard\":[[{"
            + "\"text\":\"✅ Подтвердить\","
            + "\"callback_data\":\"pay_approve_" + paymentId + "\""
            + "},{"
            + "\"text\":\"❌ Отклонить\","
            + "\"callback_data\":\"pay_reject_" + paymentId + "\""
            + "}]]}";
    }

    private static String jsonString(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }
}
