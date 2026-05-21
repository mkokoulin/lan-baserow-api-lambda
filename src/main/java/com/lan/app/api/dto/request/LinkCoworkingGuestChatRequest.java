package com.lan.app.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    name = "LinkCoworkingGuestChatRequest",
    description = "Links a Telegram chat ID to an existing coworking guest identified by phone number."
)
public record LinkCoworkingGuestChatRequest(

    @Schema(description = "Guest's phone number in international format", required = true)
    @NotNull @NotBlank String phone,

    @Schema(description = "Telegram chat ID to link to the guest", required = true)
    @NotNull Long chatId
) {}
