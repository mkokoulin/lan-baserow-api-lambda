package com.lan.app.infrastructure.baserow.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record UpdateCoworkingMeetingRoomBookingRowRequest(
    @NotNull Instant dateStart,
    @NotNull Instant dateEnd,
    @NotNull @Min(1) Integer persons,
    String comment
) {
}
