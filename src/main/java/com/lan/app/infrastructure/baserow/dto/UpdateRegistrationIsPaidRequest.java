package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateRegistrationIsPaidRequest(
    @JsonProperty("is_paid") boolean isPaid
) {}
