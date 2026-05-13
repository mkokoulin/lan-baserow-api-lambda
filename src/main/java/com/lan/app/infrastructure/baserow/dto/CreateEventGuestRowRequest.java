package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateEventGuestRowRequest(
    @NotNull @NotBlank @JsonProperty("first_name") String firstName,
    @JsonProperty("last_name") String lastName,
    @NotNull @NotBlank @JsonProperty("phone") String phone,
    @JsonProperty("telegram") String telegram,
    @JsonProperty("source") String source
) {
}
