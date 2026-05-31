package com.lan.app.api.dto.response;

import java.util.UUID;

public record CoworkingNewResponse(
    UUID id,
    String titleEn,
    String titleRu,
    String bodyEn,
    String bodyRu,
    String imageUrl,
    String link
) {
}
