package com.lan.app.api.dto.response;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    name = "EventNotificationResponse",
    description = "Event notification configuration, defining when and what message should be sent ahead of an event"
)
public record EventNotificationResponse(

    @Schema(
        description = "Unique identifier of the event notification",
        examples = "notif-123",
        required = true
    )
    String id,

    @Schema(
        description = "Number of hours before the event at which the notification should be sent",
        examples = "24",
        required = true,
        minimum = "0"
    )
    Integer leadHours,

    @Schema(
        description = "Notification message text to be delivered to the recipient",
        examples = "Reminder: your coworking tariff expires tomorrow",
        required = true
    )
    String message,

    @Schema(
        description = "Whether this notification configuration is currently active. " +
            "Inactive notifications are kept for history but will not be sent.",
        examples = "true",
        required = true
    )
    boolean active
) {
}