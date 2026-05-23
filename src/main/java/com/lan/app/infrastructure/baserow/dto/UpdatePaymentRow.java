package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdatePaymentRow(
    @JsonProperty("status") String status
) {}
