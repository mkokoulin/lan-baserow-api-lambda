package com.lan.app.domain.model;

public record EventRegistration(
    Id id,
    Id eventId,
    Id guestId,
    Integer guestCount,
    String comment,
    String source,
    boolean isPaid
) {}
