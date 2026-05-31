package com.lan.app.api.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    name = "FestivalResponse",
    description = "Festival details including its events, dates, and visibility settings"
)
public record FestivalResponse(

    @Schema(
        description = "External unique identifier of the festival",
        examples = "550e8400-e29b-41d4-a716-446655440000",
        required = true,
        format = "uuid"
    )
    UUID id,

    @Schema(
        description = "Festival name",
        examples = "Summer Fest 2026",
        required = true
    )
    String name,

    @Schema(
        description = "Short description of the festival",
        examples = "An outdoor summer festival featuring music, art, and workshops"
    )
    String description,

    @Schema(
        description = "List of external UUIDs of events included in this festival",
        required = true
    )
    List<UUID> eventsIds,

    @Schema(
        description = "Festival start date and time (ISO 8601 UTC)",
        examples = "2026-07-01T10:00:00Z",
        required = true,
        format = "date-time"
    )
    Instant dateStart,

    @Schema(
        description = "Festival end date and time (ISO 8601 UTC)",
        examples = "2026-07-05T22:00:00Z",
        required = true,
        format = "date-time"
    )
    Instant dateEnd,

    @Schema(
        description = "Whether the festival is publicly visible",
        examples = "true",
        required = true
    )
    Boolean isVisible,

    @Schema(
        description = "Whether the festival is pinned (highlighted) in listings",
        examples = "false",
        required = true
    )
    Boolean isPin,

    @Schema(
        description = "URL of the festival cover image uploaded in Baserow",
        nullable = true
    )
    String image
) {
}
