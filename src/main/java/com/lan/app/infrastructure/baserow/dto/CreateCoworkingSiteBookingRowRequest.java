package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateCoworkingSiteBookingRowRequest(
    @NotNull @JsonProperty("external_id") UUID externalId,
    @NotNull @NotBlank @JsonProperty("first_name") String firstName,
    @NotNull @NotBlank @JsonProperty("phone") String phone,
    @JsonProperty("telegram") String telegram,
    @NotNull @NotBlank @JsonProperty("tariff") String tariff,
    @NotNull @NotBlank @JsonProperty("booking_date") String bookingDate,
    @NotNull @NotBlank @JsonProperty("start_time") String startTime,
    @NotNull @NotBlank @JsonProperty("end_time") String endTime,
    @NotNull @NotBlank @JsonProperty("status") String status
) {
}
