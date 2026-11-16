package com.lan.app.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    name = "CreateBirthdayWishRequest",
    description = "Payload for submitting a birthday wish for LAN's anniversary"
)
public record CreateBirthdayWishRequest(

    @Schema(
        description = "Name of the guest leaving the wish",
        examples = "Ivan Petrov",
        required = true,
        minLength = 1
    )
    @NotNull @NotBlank String authorName,

    @Schema(description = "Telegram user id of the guest, for reference")
    String telegramUserId,

    @Schema(
        description = "The birthday wish itself",
        examples = "Happy birthday LAN! Thanks for a great year!",
        required = true,
        minLength = 1
    )
    @NotNull @NotBlank String text
) {
}
