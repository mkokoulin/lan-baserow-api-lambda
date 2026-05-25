package com.lan.app.api.dto.response;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    name = "LinkStatusResponse",
    description = "Telegram link session status for a coworking guest"
)
public record LinkStatusResponse(

    @Schema(description = "True when the Telegram account has been successfully linked for this session")
    boolean linked,

    @Schema(description = "True when the link was rejected (different Telegram account tried to log in)")
    boolean rejected,

    @Schema(description = "True when the Telegram chat ID is already registered to another account")
    boolean conflict
) {
}
