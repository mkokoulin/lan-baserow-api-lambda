package com.lan.app.domain.model;

import java.util.UUID;

public record EventRegistrationDraft(
    UUID eventId,
    UUID guestId,
    Integer guestCount,
    String comment,
    String source
) {}
