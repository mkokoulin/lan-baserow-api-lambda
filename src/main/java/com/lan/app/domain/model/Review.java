package com.lan.app.domain.model;

import java.util.UUID;

public record Review(
    UUID id,
    String authorName,
    Integer rating,
    String text,
    String createdAt
) {
}
