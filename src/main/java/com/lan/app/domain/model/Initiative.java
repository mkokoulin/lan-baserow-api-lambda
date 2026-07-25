package com.lan.app.domain.model;

public record Initiative(
    Id id,
    String titleEn,
    String titleRu,
    String descriptionEn,
    String descriptionRu,
    String imageUrl,
    String href
) {}
