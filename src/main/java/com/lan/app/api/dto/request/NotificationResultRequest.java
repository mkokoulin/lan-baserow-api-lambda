package com.lan.app.api.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NotificationResultRequest(
    @JsonProperty("guestRowId") int guestRowId,
    @JsonProperty("registrationRowId") int registrationRowId,
    @JsonProperty("status") String status,
    @JsonProperty("failureReason") String failureReason
) {}
