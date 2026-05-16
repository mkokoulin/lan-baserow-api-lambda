package com.lan.app.domain.model;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Event(
    Id id,
    String name,
    Instant dateStart,
    Instant dateEnd,
    String description,
    URI externalRegistrationUrl,
    URI registrationUrl,
    URI instagramUrl,
    boolean showForm,
    List<String> notifications,
    String comment,
    Boolean isPin
) {
}
