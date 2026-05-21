package com.lan.app.api.dto.response;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.UUID;

@Schema(
    name = "ReviewResponse",
    description = "A single review"
)
public record ReviewResponse(

    @Schema(description = "External unique identifier of the review", format = "uuid", required = true)
    UUID id,

    @Schema(description = "Name of the review author", examples = "Ivan Petrov", required = true)
    String authorName,

    @Schema(description = "Rating from 1 to 5", examples = "5", required = true, minimum = "1", maximum = "5")
    Integer rating,

    @Schema(description = "Review text")
    String text,

    @Schema(description = "ISO-8601 date-time when the review was created", examples = "2025-06-01T12:00:00Z")
    String createdAt
) {
}
