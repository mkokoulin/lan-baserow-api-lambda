package com.lan.app.telegram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramMessage {
    public Long message_id;
    public TelegramUser from;
    public TelegramChat chat;
    public String text;
}