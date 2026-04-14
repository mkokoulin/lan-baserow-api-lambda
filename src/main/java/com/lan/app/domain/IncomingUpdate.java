package com.lan.app.domain;

public record IncomingUpdate(
    Long updateId,
    Long userId,
    Long chatId,
    String text,
    String callbackData,
    UpdateType type,
    String userLanguageCode,
    String firstName,
    String username
) {
    public enum UpdateType {
        MESSAGE,
        CALLBACK
    }
}