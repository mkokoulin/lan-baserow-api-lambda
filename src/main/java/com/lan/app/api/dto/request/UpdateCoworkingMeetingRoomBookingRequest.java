package com.lan.app.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record UpdateCoworkingMeetingRoomBookingRequest(
    @NotNull Instant dateStart,
    @NotNull Instant dateEnd,
    @NotNull @Min(1) Integer persons,
    String comment
) {
}
