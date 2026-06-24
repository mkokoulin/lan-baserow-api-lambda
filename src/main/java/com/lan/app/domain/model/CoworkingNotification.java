package com.lan.app.domain.model;

import java.time.Instant;
import java.util.UUID;

public record CoworkingNotification(
    UUID externalId,
    String message,
    Instant sentAt
) {}
