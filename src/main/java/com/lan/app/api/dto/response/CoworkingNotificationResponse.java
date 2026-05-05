package com.lan.app.api.dto.response;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(
    name = "CoworkingNotificationResponse",
    description = "Coworking notification details including the message content and the time it was sent"
)
public record CoworkingNotificationResponse(

    @Schema(
        description = "External unique identifier of the notification",
        examples = "550e8400-e29b-41d4-a716-446655440000",
        required = true,
        format = "uuid"
    )
    UUID id,

    @Schema(
        description = "Notification message text",
        examples = "Your booking for meeting room A at 10:00 starts in 15 minutes",
        required = true
    )
    String message,

    @Schema(
        description = "Date and time when the notification was sent (ISO-8601, UTC)",
        examples = "2026-04-18T09:45:00Z",
        required = true,
        format = "date-time"
    )
    Instant sentAt
) {
}