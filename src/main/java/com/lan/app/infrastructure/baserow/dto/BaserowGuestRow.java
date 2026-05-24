package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BaserowGuestRow(
    @JsonProperty("id") Integer id,
    @JsonProperty("external_id") UUID externalId,
    @JsonProperty("first_name") String firstName,
    @JsonProperty("last_name") String lastName,
    @JsonProperty("phone") String phone,
    @JsonProperty("telegram") String telegram,
    @JsonProperty("telegram_chat_id") Long telegramChatId,
    @JsonProperty("source") BaserowSingleSelect source
) {}
