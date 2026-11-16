package com.lan.app.domain.model;

import java.util.UUID;

public record NewsItem(
    UUID id,
    String titleEn,
    String titleRu,
    String bodyEn,
    String bodyRu,
    String date,
    String category,
    String imageUrl,
    String link
) {}
