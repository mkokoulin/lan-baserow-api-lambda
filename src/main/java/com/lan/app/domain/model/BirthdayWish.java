package com.lan.app.domain.model;

import java.util.UUID;

public record BirthdayWish(
    UUID id,
    String authorName,
    String telegramUserId,
    String text,
    String createdAt
) {}
