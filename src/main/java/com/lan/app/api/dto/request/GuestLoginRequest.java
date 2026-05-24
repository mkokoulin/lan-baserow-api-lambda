package com.lan.app.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GuestLoginRequest(
    @NotBlank @JsonProperty("phone") String phone,
    @NotNull @JsonProperty("chat_id") Long chatId
) {
}
