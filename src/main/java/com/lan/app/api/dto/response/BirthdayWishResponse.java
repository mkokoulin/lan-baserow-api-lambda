package com.lan.app.api.dto.response;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    name = "BirthdayWishResponse",
    description = "A birthday wish left by a guest for LAN's anniversary"
)
public record BirthdayWishResponse(
    UUID id,
    String authorName,
    String telegramUserId,
    String text,
    String createdAt
) {
}
