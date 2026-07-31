package com.lan.app.domain.model;

import java.time.Instant;
import java.util.UUID;

public record EventRegistrationItem(
    UUID externalId,
    String eventName,
    Instant dateStart,
    int guestCount,
    boolean isCancelled
) {}
