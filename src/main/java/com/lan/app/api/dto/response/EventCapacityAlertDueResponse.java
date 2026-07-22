package com.lan.app.api.dto.response;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    name = "EventCapacityAlertDueResponse",
    description = "An event that just transitioned from sold-out to having available capacity again"
)
public record EventCapacityAlertDueResponse(

    @Schema(description = "Event name", required = true)
    String eventName,

    @Schema(description = "Current number of registered guests for this event", required = true)
    int registeredCount,

    @Schema(description = "Maximum capacity for this event", required = true)
    int maxCapacity
) {
}
