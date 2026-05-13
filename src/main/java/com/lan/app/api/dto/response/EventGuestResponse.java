package com.lan.app.api.dto.response;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    name = "EventGuestResponse",
    description = "Event guest details"
)
public record EventGuestResponse(

    @Schema(description = "External unique identifier (UUID) of the guest", required = true)
    String id,

    @Schema(description = "Guest's first name", required = true)
    String firstName,

    @Schema(description = "Guest's last name", nullable = true)
    String lastName,

    @Schema(description = "Guest's Telegram username", nullable = true)
    String telegram,

    @Schema(description = "Guest's phone number", required = true)
    String phone,

    @Schema(description = "")
    Long chatId
) {
}
