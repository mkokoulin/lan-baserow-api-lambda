package com.lan.app.api.dto.response;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.UUID;

@Schema(
    name = "CoworkingGuestResponse",
    description = "Coworking guest details including personal and contact information"
)
public record CoworkingGuestResponse(

    @Schema(
        description = "External unique identifier of the guest",
        example = "550e8400-e29b-41d4-a716-446655440000",
        required = true,
        format = "uuid"
    )
    UUID id,

    @Schema(
        description = "Guest's first name",
        example = "Ivan",
        required = true
    )
    String firstName,

    @Schema(
        description = "Guest's last name",
        example = "Petrov",
        required = true
    )
    String lastName,

    @Schema(
        description = "Guest's Telegram username (without the @ prefix)",
        example = "ivan_petrov",
        required = true
    )
    String telegram,

    @Schema(
        description = "Guest's phone number in international format",
        example = "+79161234567",
        required = true
    )
    String phone
) {
}