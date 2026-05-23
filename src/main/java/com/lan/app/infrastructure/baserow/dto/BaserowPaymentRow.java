package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BaserowPaymentRow(
    @JsonProperty("id") Integer id,
    @JsonProperty("external_id") UUID externalId,
    @JsonProperty("registration_id") String registrationId,
    @JsonProperty("event_name") String eventName,
    @JsonProperty("guest_name") String guestName,
    @JsonProperty("phone") String phone,
    @JsonProperty("amount") BigDecimal amount,
    @JsonProperty("proof") List<BaserowFile> proof,
    @JsonProperty("status") BaserowSingleSelect status
) {}
