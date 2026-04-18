package com.lan.app.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.UUID;

@Schema(
    name = "EventRegistrationCreateRequest",
    description = "Payload for registering a guest to an event. " +
        "Identifies the event and guest, and captures attendance details such as group size and optional comment."
)
public record EventRegistrationCreateRequest(

    @Schema(
        description = "External unique identifier of the event to register for",
        example = "550e8400-e29b-41d4-a716-446655440000",
        required = true,
        format = "uuid"
    )
    @JsonProperty("eventId") @NotNull UUID eventId,

    @Schema(
        description = "External unique identifier of the guest being registered",
        example = "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
        required = true,
        format = "uuid"
    )
    @JsonProperty("guestId") @NotNull UUID guestId,

    @Schema(
        description = "Optional free-text comment from the guest (e.g. dietary restrictions, accessibility needs, questions)",
        example = "Vegetarian meal preferred",
        nullable = true
    )
    @JsonProperty("guestComment") String guestComment,

    @Schema(
        description = "Total number of people attending under this registration, including the guest themselves. " +
            "Must be at least 1.",
        example = "2",
        required = true,
        minimum = "1"
    )
    @JsonProperty("guestCount") @Min(1) int guestCount,

    @Schema(
        description = "Optional source channel that initiated the registration " +
            "(e.g. 'website', 'mobile-app', 'telegram-bot'). Used for analytics and attribution.",
        example = "website",
        nullable = true
    )
    @JsonProperty("source") String source
) {
}