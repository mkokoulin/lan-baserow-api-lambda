package com.lan.app.domain.model;

public record Vacancy(
    Id id,
    String title,
    String deadline,
    String description,
    String href
) {}
