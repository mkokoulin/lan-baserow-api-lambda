package com.lan.app.api.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CoworkingMeetingRoomBookingResponse(
    UUID id,
    UUID guestId,
    Instant dateStart,
    Instant dateEnd,
    Integer persons,
    String comment
) {
}
