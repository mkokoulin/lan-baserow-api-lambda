package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record UpdateCoworkingMeetingRoomBookingRowRequest(
    @NotNull @JsonProperty("date_start") Instant dateStart,
    @NotNull @JsonProperty("date_end") Instant dateEnd,
    @NotNull @Min(1) @JsonProperty("persons") Integer persons,
    @JsonProperty("comment") String comment
) {
}
