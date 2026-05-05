package com.lan.app.domain.model;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Event(
    UUID id,
    String name,
    Instant dateStart,
    Instant dateEnd,
    String description,
    EventImage image,
    URI externalRegistrationUrl,
    URI registrationUrl,
    URI instagramUrl,
    EventType type,
    boolean showForm,
    List<String> notificationTime,
    String comment,
    List<EventClient> showEvent
) {
}
