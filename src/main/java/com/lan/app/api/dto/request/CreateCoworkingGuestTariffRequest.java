package com.lan.app.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateCoworkingGuestTariffRequest(
    @NotNull @JsonProperty("guestId") UUID guestId,
    @NotNull @JsonProperty("tariffId") UUID tariffId
) {}
