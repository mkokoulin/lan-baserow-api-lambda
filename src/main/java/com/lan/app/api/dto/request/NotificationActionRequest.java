package com.lan.app.api.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NotificationActionRequest(
    @JsonProperty("guestRowId") int guestRowId,
    @JsonProperty("action") String action
) {}
