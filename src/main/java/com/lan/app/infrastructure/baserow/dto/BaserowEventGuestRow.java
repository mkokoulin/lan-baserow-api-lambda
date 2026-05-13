package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BaserowEventGuestRow(
    @NotNull @JsonProperty("id") Integer id,
    @NotNull @JsonProperty("external_id") UUID externalId,
    @NotNull @NotBlank @JsonProperty("first_name") String firstName,
    @JsonProperty("last_name") String lastName,
    @JsonProperty("telegram") String telegram,
    @NotNull @NotBlank @JsonProperty("phone") String phone,
    @JsonProperty("source") BaserowSingleSelect source,
    @JsonProperty("telegram_chat_id") Long telegramChatId
) {}
