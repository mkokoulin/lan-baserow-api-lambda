package com.lan.app.service.command;

import java.util.UUID;

public record CreateEventRegistrationCommand(
    UUID eventId,
    UUID guestId,
    String comment,
    Integer guestCount,
    String source
) {}