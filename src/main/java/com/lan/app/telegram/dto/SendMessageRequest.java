package com.lan.app.telegram.dto;

public class SendMessageRequest {
    public Long chat_id;
    public String text;

    public SendMessageRequest() {
    }

    public SendMessageRequest(Long chatId, String text) {
        this.chat_id = chatId;
        this.text = text;
    }
}