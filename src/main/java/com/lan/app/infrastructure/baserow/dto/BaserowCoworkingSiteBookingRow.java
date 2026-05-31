package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BaserowCoworkingSiteBookingRow(
    @NotNull @JsonProperty("id") Integer id,
    @NotNull @JsonProperty("external_id") UUID externalId,
    @JsonProperty("first_name") String firstName,
    @JsonProperty("phone") String phone,
    @JsonProperty("telegram") String telegram,
    @JsonProperty("booking_date") String bookingDate,
    @JsonProperty("start_time") String startTime,
    @JsonProperty("end_time") String endTime
) {
}
