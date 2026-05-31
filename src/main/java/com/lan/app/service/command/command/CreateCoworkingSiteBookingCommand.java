package com.lan.app.service.command;

import java.util.UUID;

public record CreateCoworkingSiteBookingCommand(
    UUID externalId,
    String firstName,
    String phone,
    String telegram,
    String tariff,
    String bookingDate,
    String startTime,
    String endTime
) {
}
