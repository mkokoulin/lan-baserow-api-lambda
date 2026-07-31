package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateRegistrationCancelledRequest(
    @JsonProperty("is_cancelled") boolean isCancelled
) {}
