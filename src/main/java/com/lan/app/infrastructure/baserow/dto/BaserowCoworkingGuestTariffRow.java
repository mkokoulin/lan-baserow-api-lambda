package com.lan.app.infrastructure.baserow.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record BaserowCoworkingGuestTariffRow(
    @NotNull @JsonProperty("id") Integer id,
    @NotNull @JsonProperty("external_id") UUID externalId,
    @NotNull @JsonProperty("tariff_id") List<@Valid BaserowLinkToTable> tariffId,
    @NotNull @JsonProperty("guest_id") List<@Valid BaserowLinkToTable> guestId,
    @NotNull @JsonProperty("date_start") Instant dateStart,
    @NotNull @JsonProperty("date_end") Instant dateEnd,
    @NotNull @JsonProperty("days_used") Integer daysUsed,
    @NotNull @JsonProperty("active") Boolean active
) {
   
}
