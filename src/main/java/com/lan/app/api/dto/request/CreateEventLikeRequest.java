package com.lan.app.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    name = "CreateEventLikeRequest",
    description = "Payload for liking or unliking an event"
)
public record CreateEventLikeRequest(

    @Schema(
        description = "Anonymous, browser-generated identifier used to dedupe one like per visitor",
        examples = "8f14e45f-ceea-4a9e-8c96-1b1f3c0a7e11",
        required = true,
        minLength = 1
    )
    @NotNull @NotBlank String anonId
) {
}
