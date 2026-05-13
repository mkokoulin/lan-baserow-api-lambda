package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateGuestTelegramChatIdRequest(
    @JsonProperty("telegram_chat_id") Long telegramChatId
) {
}
