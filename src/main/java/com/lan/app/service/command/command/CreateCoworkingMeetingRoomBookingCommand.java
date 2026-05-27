package com.lan.app.service.command;

import java.time.Instant;
import java.util.UUID;

public record CreateCoworkingMeetingRoomBookingCommand(
    UUID guestId,
    Instant dateStart,
    Instant dateEnd,
    Integer persons,
    String comment
) {
}
