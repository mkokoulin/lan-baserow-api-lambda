package com.lan.app.api.dto.response;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    name = "InitiativeResponse",
    description = "Community/social initiative published on the initiatives page"
)
public record InitiativeResponse(

    @Schema(
        description = "External unique identifier of the initiative",
        examples = "550e8400-e29b-41d4-a716-446655440000",
        required = true,
        format = "uuid"
    )
    UUID id,

    @Schema(
        description = "Initiative title (English)",
        examples = "Plastic collection and recycling",
        required = true
    )
    String titleEn,

    @Schema(
        description = "Initiative title (Russian)",
        examples = "Сбор пластика и переработка отходов",
        required = true
    )
    String titleRu,

    @Schema(
        description = "Full initiative description (English). May contain sanitized HTML markup " +
            "(e.g. <p>, <strong>, <a>, <ul>/<li>) produced by Baserow's rich text editor; " +
            "clients must sanitize before rendering.",
        required = true
    )
    String descriptionEn,

    @Schema(
        description = "Full initiative description (Russian). May contain sanitized HTML markup " +
            "(e.g. <p>, <strong>, <a>, <ul>/<li>) produced by Baserow's rich text editor; " +
            "clients must sanitize before rendering.",
        required = true
    )
    String descriptionRu,

    @Schema(
        description = "Cover image URL",
        nullable = true
    )
    String imageUrl,

    @Schema(
        description = "Optional link with more details about the initiative",
        nullable = true
    )
    String href
) {
}
