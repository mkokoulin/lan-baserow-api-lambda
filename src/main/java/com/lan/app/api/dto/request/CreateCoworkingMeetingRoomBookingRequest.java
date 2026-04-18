package com.lan.app.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record CreateCoworkingMeetingRoomBookingRequest(
    @NotNull @JsonProperty("guestId") UUID guestId,
    @NotNull @JsonProperty("dateStart") Instant dateStart,
    @NotNull @JsonProperty("dateEnd") Instant dateEnd,
    @NotNull @JsonProperty("persons") Integer persons,
    @JsonProperty("comment") String comment
) {
}
