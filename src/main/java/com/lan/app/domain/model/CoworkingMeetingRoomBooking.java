package com.lan.app.domain.model;

import java.time.Instant;
import java.util.UUID;

public record CoworkingMeetingRoomBooking(
    UUID externalId,
    UUID guestId,
    Instant dateStart,
    Instant dateEnd,
    Integer persons,
    String comment
) {}
