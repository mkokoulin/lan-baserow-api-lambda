package com.lan.app.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    name = "CreateCoworkingGuestRequest",
    description = "Payload for creating a new coworking guest. All fields are required."
)
public record CreateCoworkingGuestRequest(

    @Schema(
        description = "Guest's first name. Must be a non-blank string.",
        example = "Ivan",
        required = true,
        minLength = 1
    )
    @NotNull @NotBlank @JsonProperty("firstName") String firstName,

    @Schema(
        description = "Guest's last name. Must be a non-blank string.",
        example = "Petrov",
        required = true,
        minLength = 1
    )
    @NotNull @NotBlank @JsonProperty("lastName") String lastName,

    @Schema(
        description = "Guest's Telegram username (without the @ prefix). Must be a non-blank string.",
        example = "ivan_petrov",
        required = true,
        minLength = 1
    )
    @NotNull @NotBlank @JsonProperty("telegram") String telegram,

    @Schema(
        description = "Guest's phone number in international format. Must be a non-blank string.",
        example = "+79161234567",
        required = true,
        minLength = 1
    )
    @NotNull @NotBlank @JsonProperty("phone") String phone
) {
}