package com.lan.app.infrastructure.baserow.dto;
import com.baserow.dto.BaserowSingleSelect;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BaserowCoworkingTariffRow(
    @NotNull @JsonProperty("id") Integer id,
    @NotNull @NotBlank @JsonProperty("external_id") UUID externalId,
    @NotNull @NotBlank @JsonProperty("name") String name,
    @NotNull @JsonProperty("price") Integer price,
    @Nullable @JsonProperty("meeting_room") BaserowSingleSelect meetingRoom,
    @NotNull @JsonProperty("fixed_desc") boolean fixedDesc,
    @NotNull @JsonProperty("filter_coffee_or_tea") boolean filterCoffeeOrTea,
    @NotNull @JsonProperty("printout_scan") boolean printoutScan,
    @NotNull @JsonProperty("luggage_storage") boolean luggageStorage,
    @Nullable @JsonProperty("type") BaserowSingleSelect type
) {
}
