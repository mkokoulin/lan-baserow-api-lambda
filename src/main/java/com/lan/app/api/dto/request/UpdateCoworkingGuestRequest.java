package com.lan.app.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    name = "UpdateCoworkingGuestRequest",
    description = "Payload for updating an existing coworking guest. " +
        "The 'telegram' field is optional; if omitted, the current value is kept."
)
public record UpdateCoworkingGuestRequest(

    @Schema(
        description = "Guest's first name. Must be a non-blank string.",
        example = "Ivan",
        required = true,
        minLength = 1
    )
    @NotNull @NotBlank String firstName,

    @Schema(
        description = "Guest's last name. Must be a non-blank string.",
        example = "Petrov",
        required = true,
        minLength = 1
    )
    @NotNull @NotBlank String lastName,

    @Schema(
        description = "Guest's phone number in international format. Must be a non-blank string.",
        example = "+79161234567",
        required = true,
        minLength = 1
    )
    @NotNull @NotBlank String phone,

    @Schema(
        description = "Guest's Telegram username (without the @ prefix). Optional — if omitted, the current value is preserved.",
        example = "ivan_petrov",
        required = false,
        nullable = true
    )
    String telegram
) {
}