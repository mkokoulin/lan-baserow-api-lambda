package com.lan.app.api.dto.response;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    name = "EventTypeResponse",
    description = "Event type definition (e.g. 'meetup', 'workshop') with its display color for UI rendering"
)
public record EventTypeResponse(

    @Schema(
        description = "Unique numeric identifier of the event type",
        examples = "1",
        required = true,
        minimum = "1"
    )
    int id,

    @Schema(
        description = "Human-readable name of the event type (e.g. 'meetup', 'workshop', 'conference')",
        examples = "meetup",
        required = true
    )
    String value,

    @Schema(
        description = "Color associated with the event type, used for visual differentiation in the UI. " +
            "Typically a CSS color name or hex code.",
        examples = "#FF5733",
        required = true
    )
    String color
) {
}