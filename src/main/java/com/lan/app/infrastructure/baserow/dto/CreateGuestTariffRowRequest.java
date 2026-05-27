package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateGuestTariffRowRequest(
    @NotNull @JsonProperty("guest_id") List<Integer> guestId,
    @NotNull @JsonProperty("tariff_id") List<Integer> tariffId
) {}
