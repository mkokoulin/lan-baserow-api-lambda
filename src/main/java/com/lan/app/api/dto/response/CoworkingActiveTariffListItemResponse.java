package com.lan.app.api.dto.response;

import java.time.Instant;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    name = "CoworkingActiveTariffListItemResponse",
    description = "Lightweight representation of an active coworking tariff, used in list views"
)
public record CoworkingActiveTariffListItemResponse(

    @Schema(
        description = "External unique identifier of the active tariff",
        example = "550e8400-e29b-41d4-a716-446655440000",
        required = true,
        format = "uuid"
    )
    UUID id,

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
    Instant dateEnd
) {
}
