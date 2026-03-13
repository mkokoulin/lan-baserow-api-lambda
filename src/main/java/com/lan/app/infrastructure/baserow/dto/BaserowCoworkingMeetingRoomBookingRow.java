package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BaserowCoworkingMeetingRoomBookingRow(
    @NotNull @JsonProperty("id") Integer id,
    @NotNull @JsonProperty("external_id") UUID externalId,
    @NotNull @JsonProperty("date_start") Instant dateStart,
    @NotNull @JsonProperty("date_end") Instant dateEnd,
    @NotNull @JsonProperty("guest_id") List<@Valid BaserowLinkToTable> guestId,
    @NotNull @JsonProperty("persons") Integer persons,
    @JsonProperty("comment") String comment
) {
}
