package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateCoworkingSiteBookingRowRequest(
    @NotNull @NotBlank @JsonProperty("first_name") String firstName,
    @NotNull @JsonProperty("phone") String phone,
    @JsonProperty("telegram") String telegram,
    @NotNull @JsonProperty("tariff") List<Integer> tariffRowIds,
    @NotNull @NotBlank @JsonProperty("booking_date") String bookingDate,
    @JsonProperty("start_time") String startTime,
    @JsonProperty("end_time") String endTime,
    @JsonProperty("status") String status
) {
}
