package com.lan.app.api.dto.response;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.UUID;

@Schema(
    name = "CoworkingTariffResponse",
    description = "Coworking tariff details including price and included services"
)
public record CoworkingTariffResponse(

    @Schema(
        description = "External unique identifier of the tariff",
        examples = "550e8400-e29b-41d4-a716-446655440000",
        required = true,
        format = "uuid"
    )
    UUID id,

    @Schema(
        description = "Tariff name",
        examples = "Day Pass",
        required = true
    )
    String name,

    @Schema(
        description = "Tariff price in the base currency (e.g. RUB), as a whole number",
        examples = "1500",
        required = true,
        minimum = "0"
    )
    Integer price,

    @Schema(
        description = "Meeting room access description or allowance included in the tariff " +
            "(e.g. 'Unlimited', '2 hours per day', 'Not included')",
        examples = "2 hours per day"
    )
    String meetingRoom,

    @Schema(
        description = "Whether a fixed (dedicated) desk is included in the tariff",
        examples = "true",
        required = true
    )
    boolean fixedDesk,

    @Schema(
        description = "Whether complimentary filter coffee and tea are included in the tariff",
        examples = "true",
        required = true
    )
    boolean filterCoffeeAndTea,

    @Schema(
        description = "Whether printing and scanning services are included in the tariff",
        examples = "false",
        required = true
    )
    boolean printoutScan,

    @Schema(
        description = "Whether luggage storage is available with this tariff",
        examples = "true",
        required = true
    )
    boolean luggageStorage
) {
}