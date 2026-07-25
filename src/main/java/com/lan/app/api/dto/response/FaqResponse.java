package com.lan.app.api.dto.response;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    name = "FaqResponse",
    description = "Frequently asked question published on the FAQ page"
)
public record FaqResponse(

    @Schema(
        description = "External unique identifier of the FAQ entry",
        examples = "550e8400-e29b-41d4-a716-446655440000",
        required = true,
        format = "uuid"
    )
    UUID id,

    @Schema(
        description = "Question (English)",
        examples = "What are your working hours?",
        required = true
    )
    String questionEn,

    @Schema(
        description = "Question (Russian)",
        examples = "Какой у вас график работы?",
        required = true
    )
    String questionRu,

    @Schema(
        description = "Answer (English). May contain sanitized HTML markup " +
            "(e.g. <p>, <strong>, <a>, <ul>/<li>) produced by Baserow's rich text editor; " +
            "clients must sanitize before rendering.",
        required = true
    )
    String answerEn,

    @Schema(
        description = "Answer (Russian). May contain sanitized HTML markup " +
            "(e.g. <p>, <strong>, <a>, <ul>/<li>) produced by Baserow's rich text editor; " +
            "clients must sanitize before rendering.",
        required = true
    )
    String answerRu,

    @Schema(
        description = "Display order (ascending). Lower numbers appear first",
        nullable = true
    )
    Integer position
) {
}
