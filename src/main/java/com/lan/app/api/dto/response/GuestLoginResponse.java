package com.lan.app.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GuestLoginResponse(
    @JsonProperty("token") String token
) {
}
