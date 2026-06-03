package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateEventNotificationStatusRequest(
    @JsonProperty("status") String status
) {}
