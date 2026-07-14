package com.lan.app.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RegistrationConfirmResponse(
    @JsonProperty("event_name") String eventName
) {
}
