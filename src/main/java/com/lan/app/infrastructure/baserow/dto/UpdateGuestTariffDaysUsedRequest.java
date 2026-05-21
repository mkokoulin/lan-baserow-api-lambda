package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateGuestTariffDaysUsedRequest(
    @NotNull @Min(0) @JsonProperty("days_used") Integer daysUsed
) {
}
