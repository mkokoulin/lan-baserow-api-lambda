package com.lan.app.api.dto.request;

import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    name = "LinkCoworkingGuestChatByIdRequest",
    description = "Links a Telegram chat ID directly to a coworking guest by their UUID."
)
public record LinkCoworkingGuestChatByIdRequest(

    @Schema(description = "Telegram chat ID to link", required = true)
    @NotNull Long chatId
) {}
