package com.lan.app.api.dto.response;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(
    name = "EventResponse",
    description = "Event details including schedule, registration links, and notification configuration"
)
public record EventResponse(

    @Schema(
        description = "External unique identifier of the event",
        example = "550e8400-e29b-41d4-a716-446655440000",
        required = true,
        format = "uuid"
    )
    UUID id,

    @Schema(
        description = "Event name",
        example = "Startup Pitch Night",
        required = true
    )
    String name,

    @Schema(
        description = "Date and time when the event starts, as a string in the format defined by the source system " +
            "(typically ISO-8601)",
        example = "2026-05-10T18:00:00Z",
        required = true
    )
    String dateStart,

    @Schema(
        description = "Date and time when the event ends, as a string in the format defined by the source system " +
            "(typically ISO-8601). Must be later than dateStart.",
        example = "2026-05-10T21:00:00Z",
        required = true
    )
    String dateEnd,

    @Schema(
        description = "Free-text description of the event, may contain plain text or formatted content",
        example = "An evening of pitches from emerging startups, followed by networking drinks.",
        nullable = true
    )
    String description,

    @Schema(
        description = "External URL for event registration on a third-party platform (e.g. Timepad, Eventbrite). " +
            "Used when registration is handled outside this system.",
        example = "https://timepad.ru/event/1234567",
        nullable = true,
        format = "uri"
    )
    String externalRegistrationUrl,

    @Schema(
        description = "Internal registration URL within this system",
        example = "https://coworking.example.com/events/550e8400/register",
        nullable = true,
        format = "uri"
    )
    String registrationUrl,

    @Schema(
        description = "Link to the event's Instagram post or story",
        example = "https://instagram.com/p/Abc123Def456",
        nullable = true,
        format = "uri"
    )
    String instagramUrl,

    @Schema(
        description = "Event type or category (e.g. 'meetup', 'workshop', 'conference')",
        example = "meetup",
        nullable = true
    )
    String type,

    @Schema(
        description = "Whether the internal registration form should be shown to users. " +
            "When false, users are expected to register via externalRegistrationUrl.",
        example = "true",
        required = true
    )
    boolean showForm,

    @Schema(
        description = "List of notification identifiers configured for this event. " +
            "Each identifier references an EventNotificationResponse and defines when a reminder should be sent.",
        example = "[\"notif-123\", \"notif-456\"]",
        nullable = true,
        type = SchemaType.ARRAY,
        implementation = String.class
    )
    List<String> notifications,

    @Schema(
        description = "Internal comment or note about the event, not intended for public display",
        example = "Need to confirm catering by May 1",
        nullable = true
    )
    String comment,

    @Schema(
        description = "List of channels or contexts where this event should be displayed " +
            "(e.g. 'website', 'mobile-app', 'telegram-bot')",
        example = "[\"website\", \"mobile-app\"]",
        nullable = true,
        type = SchemaType.ARRAY,
        implementation = String.class
    )
    List<String> showEvent
) {
}