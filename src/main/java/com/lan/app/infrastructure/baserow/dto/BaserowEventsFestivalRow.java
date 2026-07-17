package com.lan.app.infrastructure.baserow.dto;
import com.baserow.dto.BaserowFile;
import com.baserow.dto.BaserowSelectOption;

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
    @JsonProperty("date_start") String dateStart,
    @JsonProperty("date_end") String dateEnd,
    @JsonProperty("is_visible") boolean isVisible,
    @JsonProperty("position") Integer position,
    @JsonProperty("show_on_home") boolean showOnHome,
    @JsonProperty("image") List<BaserowFile> image
) {}
