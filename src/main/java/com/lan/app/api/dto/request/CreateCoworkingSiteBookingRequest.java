package com.lan.app.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateCoworkingSiteBookingRequest(
    @NotNull @JsonProperty("externalId") UUID externalId,
    @NotNull @NotBlank @JsonProperty("firstName") String firstName,
    @NotNull @NotBlank @JsonProperty("phone") String phone,
    @JsonProperty("telegram") String telegram,
    @NotNull @NotBlank @JsonProperty("tariff") String tariff,
    @NotNull @NotBlank @JsonProperty("bookingDate") String bookingDate,
    @NotNull @NotBlank @JsonProperty("startTime") String startTime,
    @NotNull @NotBlank @JsonProperty("endTime") String endTime
) {
}
