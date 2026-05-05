package com.lan.app.api.dto.response;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    name = "EventRegistrationResponse",
    description = "Event registration details, linking a guest to an event and recording their attendance information"
)
public record EventRegistrationResponse(

    @Schema(
        description = "Unique identifier of the event registration",
        examples = "reg-123",
        required = true
    )
    String id,

    @Schema(
        description = "Identifier of the event the guest is registered for",
        examples = "event-456",
        required = true
    )
    String eventId,

    @Schema(
        description = "Identifier of the guest who registered for the event",
        examples = "guest-789",
        required = true
    )
    String guestId,

    @Schema(
        description = "Optional free-text comment from the guest (e.g. dietary restrictions, accessibility needs, questions)",
        examples = "Vegetarian meal preferred",
        nullable = true
    )
    String guestComment,

    @Schema(
        description = "Total number of people attending under this registration, including the guest themselves",
        examples = "2",
        required = true,
        minimum = "1"
    )
    int guestCount
) {
}