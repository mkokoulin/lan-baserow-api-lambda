package com.lan.app.api.dto.response;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(
    name = "CoworkingActiveTariffResponse",
    description = "Detailed representation of an active coworking tariff, including guest and tariff references"
)
public record CoworkingActiveTariffResponse(

    @Schema(
        description = "External unique identifier of the active tariff",
        example = "550e8400-e29b-41d4-a716-446655440000",
        required = true,
        format = "uuid"
    )
    UUID id,

    @Schema(
        description = "External unique identifier of the guest this active tariff belongs to",
        example = "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
        required = true,
        format = "uuid"
    )
    UUID guestId,

    @Schema(
        description = "Number of days the tariff has already been used",
        example = "5",
        required = true,
        minimum = "0"
    )
    Integer daysUsed,

    @Schema(
        description = "Date and time when the tariff became active (ISO-8601, UTC)",
        example = "2026-01-15T10:00:00Z",
        required = true,
        format = "date-time"
    )
    Instant dateStart,

    @Schema(
        description = "Date and time when the tariff expires (ISO-8601, UTC)",
        example = "2026-12-31T23:59:59Z",
        required = true,
        format = "date-time"
    )
    Instant dateEnd,

    @Schema(
        description = "External unique identifier of the underlying tariff definition",
        example = "7c9e6679-7425-40de-944b-e07fc1f90ae7",
        required = true,
        format = "uuid"
    )
    UUID tariffId
) {
}