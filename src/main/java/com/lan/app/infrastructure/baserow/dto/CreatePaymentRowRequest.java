package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record CreatePaymentRowRequest(
    @JsonProperty("registration_id") List<Integer> registrationId,
    @JsonProperty("event_name") String eventName,
    @JsonProperty("guest_name") String guestName,
    @JsonProperty("phone") String phone,
    @JsonProperty("amount") BigDecimal amount,
    @JsonProperty("proof") List<Map<String, String>> proof,
    @JsonProperty("status") String status
) {}
