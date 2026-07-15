package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateEventNotificationResultRowRequest(
    @JsonProperty("event_notification") List<Integer> eventNotification,
    @JsonProperty("guest") List<Integer> guest,
    @JsonProperty("status") String status,
    @JsonProperty("failure_reason") String failureReason,
    @JsonProperty("sent_at") String sentAt,
    @JsonProperty("event_registrations") List<Integer> eventRegistrations
) {}
