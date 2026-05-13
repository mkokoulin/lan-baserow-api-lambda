package com.lan.app.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record BotRegistrationDto(
    @JsonProperty("event_name") String eventName,
    @JsonProperty("date_start") Instant dateStart
) {
}
