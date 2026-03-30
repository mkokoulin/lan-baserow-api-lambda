package com.lan.app.telegram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramUpdate {
    public Long update_id;
    public TelegramMessage message;
    public TelegramCallbackQuery callback_query;
}
