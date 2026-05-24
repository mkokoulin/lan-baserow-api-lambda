package com.lan.app.api.dto.request;

import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    name = "UnlinkCoworkingGuestChatRequest",
    description = "Removes the Telegram chat ID link from the guest identified by the given chat ID."
)
public record UnlinkCoworkingGuestChatRequest(

    @Schema(description = "Telegram chat ID to unlink", required = true)
    @NotNull Long chatId
) {}
