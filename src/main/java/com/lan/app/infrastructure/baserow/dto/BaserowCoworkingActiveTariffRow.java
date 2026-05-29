package com.lan.app.infrastructure.baserow.dto;
import com.baserow.dto.BaserowLinkToTable;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BaserowCoworkingActiveTariffRow(
    @NotNull @JsonProperty("id") Integer id,
    @NotNull @JsonProperty("external_id") UUID externalId,
    @NotNull @JsonProperty("guest_id") List<@Valid BaserowLinkToTable> guestId,
    @NotNull @JsonProperty("days_used") Integer daysUsed,
    @NotNull @JsonProperty("date_start") Instant dateStart,
    @NotNull @JsonProperty("date_end") Instant dateEnd,
    @NotNull @JsonProperty("tariff_id") List<@Valid BaserowLinkToTable> tariffId
) {}
