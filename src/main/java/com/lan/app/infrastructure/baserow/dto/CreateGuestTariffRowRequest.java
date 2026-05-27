package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateGuestTariffRowRequest(
    @NotNull @JsonProperty("guest_id") List<Integer> guestId,
    @NotNull @JsonProperty("tariff_id") List<Integer> tariffId,
    @JsonProperty("status") String status
) {
    public CreateGuestTariffRowRequest(List<Integer> guestId, List<Integer> tariffId) {
        this(guestId, tariffId, "PENDING");
    }
}
