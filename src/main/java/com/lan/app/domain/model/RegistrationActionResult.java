package com.lan.app.domain.model;

import java.time.Instant;

public record RegistrationActionResult(
    String eventName,
    Instant dateStart,
    int previousGuestCount,
    int guestCount,
    String guestFirstName,
    String guestLastName,
    String guestPhone,
    String guestTelegram
) {}
