package com.lan.app.infrastructure.baserow.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BaserowEventsFestivalRow(
    @NotNull @JsonProperty("id") Integer id,
    @NotNull @JsonProperty("external_id") UUID externalId,
    @NotBlank @JsonProperty("name") String name,
    @NotBlank @JsonProperty("description") String description,
    @JsonProperty("events_ids") List<BaserowSelectOption> eventsIds,
    @NotNull @JsonProperty("date_start") Instant dateStart,
    @NotNull @JsonProperty("date_end") Instant dateEnd,
    @JsonProperty("is_visible") boolean isVisible,
    @JsonProperty("is_pin") boolean isPin
) {}
