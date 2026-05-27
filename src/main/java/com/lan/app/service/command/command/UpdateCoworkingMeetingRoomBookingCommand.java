package com.lan.app.service.command;

import java.time.Instant;

public record UpdateCoworkingMeetingRoomBookingCommand(
    Instant dateStart,
    Instant dateEnd,
    Integer persons,
    String comment
) {
}
