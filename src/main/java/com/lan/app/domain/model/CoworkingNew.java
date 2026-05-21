package com.lan.app.domain.model;

import java.util.UUID;

public record CoworkingNew(
    UUID id,
    String titleEn,
    String titleRu,
    String bodyEn,
    String bodyRu,
    String link
){}