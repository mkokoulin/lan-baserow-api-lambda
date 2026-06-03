package com.lan.app.infrastructure.baserow.dto;

import com.baserow.dto.BaserowSingleSelect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BaserowNotificationRow(
    @NotNull @JsonProperty("id") Integer id,
    @NotNull @JsonProperty("external_id") UUID externalId,
    @JsonProperty("lead_time") List<BaserowSingleSelect> leadTime,
    @JsonProperty("message") String message,
    @JsonProperty("comment") String comment,
    @JsonProperty("active") boolean active
) {}
