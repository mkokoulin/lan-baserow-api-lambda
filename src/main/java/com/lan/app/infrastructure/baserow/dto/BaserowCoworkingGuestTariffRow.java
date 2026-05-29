package com.lan.app.infrastructure.baserow.dto;
import com.baserow.dto.BaserowSingleSelect;
import com.baserow.dto.BaserowLinkToTable;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BaserowCoworkingGuestTariffRow(
    @NotNull @JsonProperty("id") Integer id,
    @NotNull @JsonProperty("external_id") UUID externalId,
    @NotNull @JsonProperty("tariff_id") List<@Valid BaserowLinkToTable> tariffId,
    @NotNull @JsonProperty("guest_id") List<@Valid BaserowLinkToTable> guestId,
    @NotNull @JsonProperty("days_used") Integer daysUsed,
    @JsonProperty("status") BaserowSingleSelect status
) {

}
