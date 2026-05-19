package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BaserowCoworkingGuestRow(
    @NotNull @JsonProperty("id") Integer id,
    @NotNull @JsonProperty("telegram_chat_id") Long telegramChatId,
    @NotNull @JsonProperty("external_id") UUID externalId,
    @NotNull @NotBlank @JsonProperty("first_name") String firstName,
    @NotNull @NotBlank @JsonProperty("last_name") String lastName,
    @NotNull @NotBlank @JsonProperty("phone") String phone,
    @NotNull @NotBlank @JsonProperty("telegram") String telegram
) {
}
