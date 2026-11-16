package com.lan.app.api.dto.response;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    name = "NewsItemResponse",
    description = "News item published on the news page"
)
public record NewsItemResponse(
    UUID id,
    String titleEn,
    String titleRu,

    @Schema(
        description = "Full item body (English). May contain sanitized HTML markup " +
            "(e.g. <p>, <strong>, <a>, <ul>/<li>) produced by Baserow's rich text editor; " +
            "clients must sanitize before rendering."
    )
    String bodyEn,

    @Schema(
        description = "Full item body (Russian). May contain sanitized HTML markup " +
            "(e.g. <p>, <strong>, <a>, <ul>/<li>) produced by Baserow's rich text editor; " +
            "clients must sanitize before rendering."
    )
    String bodyRu,

    String date,
    String category,
    String imageUrl,
    String link
) {
}
