package com.lan.app.infrastructure.baserow.dto;

import com.baserow.dto.BaserowLinkToTable;
import com.baserow.dto.BaserowSingleSelect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BaserowEventNotificationResultRow(
    @JsonProperty("id") Integer id,
    @JsonProperty("event_notification") List<BaserowLinkToTable> eventNotification,
    @JsonProperty("guest") List<BaserowLinkToTable> guest,
    @JsonProperty("status") BaserowSingleSelect status,
    @JsonProperty("action") BaserowSingleSelect action,
    @JsonProperty("failure_reason") String failureReason,
    @JsonProperty("sent_at") String sentAt
) {}
