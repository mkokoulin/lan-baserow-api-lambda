package com.lan.app.domain.model;

import java.util.UUID;

public record CoworkingSiteBooking(
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
