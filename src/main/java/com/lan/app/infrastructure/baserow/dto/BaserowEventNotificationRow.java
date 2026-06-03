package com.lan.app.infrastructure.baserow.dto;

import com.baserow.dto.BaserowLinkToTable;
import com.baserow.dto.BaserowSingleSelect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BaserowEventNotificationRow(
    @NotNull @JsonProperty("id") Integer id,
    @NotNull @JsonProperty("external_id") UUID externalId,
    @JsonProperty("status") BaserowSingleSelect status,
    @JsonProperty("notifications") List<BaserowLinkToTable> notifications,
    @JsonProperty("event_id") List<BaserowLinkToTable> eventId,
    @JsonProperty("active") boolean active
) {}
