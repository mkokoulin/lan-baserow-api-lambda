package com.lan.app.api.dto.response;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(
    name = "CoworkingMeetingRoomBookingResponse",
    description = "Meeting room booking details, including the guest who made the booking and the reserved time slot"
)
public record CoworkingMeetingRoomBookingResponse(

    @Schema(
        description = "External unique identifier of the meeting room booking",
        example = "550e8400-e29b-41d4-a716-446655440000",
        required = true,
        format = "uuid"
    )
    UUID id,

    @Schema(
        description = "External unique identifier of the guest who made the booking",
        example = "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
        required = true,
        format = "uuid"
    )
    UUID guestId,

    @Schema(
        description = "Date and time when the booking starts (ISO-8601, UTC)",
        example = "2026-04-20T09:00:00Z",
        required = true,
        format = "date-time"
    )
    Instant dateStart,

    @Schema(
        description = "Date and time when the booking ends (ISO-8601, UTC). Must be later than dateStart.",
        example = "2026-04-20T11:00:00Z",
        required = true,
        format = "date-time"
    )
    Instant dateEnd,

    @Schema(
        description = "Number of people expected to attend the meeting",
        example = "4",
        required = true,
        minimum = "1"
    )
    Integer persons,

    @Schema(
        description = "Optional free-text comment or note about the booking (e.g. agenda, special requirements)",
        example = "Client presentation, projector required",
        nullable = true
    )
    String comment
) {
}