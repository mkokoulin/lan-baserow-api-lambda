package com.lan.app.domain.model;

public record Initiative(
    Id id,
    String title,
    String description,
    String imageUrl,
    String href
) {}
