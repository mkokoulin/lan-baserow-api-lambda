package com.lan.app.domain.model;

import java.time.Instant;

public record EventRegistrationItem(
    String eventName,
    Instant dateStart
) {
}
