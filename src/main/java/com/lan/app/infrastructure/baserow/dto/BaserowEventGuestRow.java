package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BaserowEventGuestRow(
    @JsonProperty("id") Integer id,
    @JsonProperty("external_id") UUID externalId,
    @JsonProperty("first_name") String firstName,
    @JsonProperty("last_name") String lastName,
    @JsonProperty("telegram") String telegram,
    @JsonProperty("phone") String phone,
    @JsonProperty("source") BaserowSingleSelect source,
    @JsonProperty("telegram_chat_id") Long chatId
) {}
