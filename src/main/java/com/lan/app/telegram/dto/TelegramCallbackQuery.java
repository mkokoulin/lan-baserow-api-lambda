package com.lan.app.telegram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramCallbackQuery {
    public String id;
    public TelegramUser from;
    public String data;
    public TelegramMessage message;
}