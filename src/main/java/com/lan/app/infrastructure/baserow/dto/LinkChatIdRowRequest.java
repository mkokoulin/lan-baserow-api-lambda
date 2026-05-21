package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LinkChatIdRowRequest(
    @JsonProperty("telegram_chat_id") Long telegramChatId
) {}
