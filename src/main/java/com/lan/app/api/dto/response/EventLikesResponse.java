package com.lan.app.api.dto.response;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    name = "EventLikesResponse",
    description = "Current like count for an event"
)
public record EventLikesResponse(

    @Schema(
        description = "Number of likes the event currently has",
        examples = "12",
        required = true
    )
    long count
) {
}
