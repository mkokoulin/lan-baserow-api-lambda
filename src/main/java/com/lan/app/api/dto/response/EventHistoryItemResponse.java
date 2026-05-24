package com.lan.app.api.dto.response;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;

@Schema(
    name = "EventHistoryItemResponse",
    description = "A single event the guest has registered for"
)
public record EventHistoryItemResponse(

    @Schema(description = "Name of the event", examples = "Yoga workshop", required = true)
    String eventName,

    @Schema(description = "Event start date-time in UTC", format = "date-time", required = true)
    Instant dateStart
) {}
