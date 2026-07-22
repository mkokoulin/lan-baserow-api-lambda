package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateEventNotificationRowRequest(
    @JsonProperty("event_id") List<Integer> eventId,
    @JsonProperty("active") Boolean active
) {}
