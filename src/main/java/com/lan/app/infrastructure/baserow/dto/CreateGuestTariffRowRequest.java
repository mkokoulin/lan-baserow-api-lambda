package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateGuestTariffRowRequest(
    @NotNull @JsonProperty("external_id") UUID externalId,
    @NotNull @JsonProperty("guest_id") List<Integer> guestId,
    @NotNull @JsonProperty("tariff_id") List<Integer> tariffId,
    @NotNull @JsonProperty("days_used") Integer daysUsed
) {}
