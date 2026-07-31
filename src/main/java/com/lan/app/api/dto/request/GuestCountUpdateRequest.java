package com.lan.app.api.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GuestCountUpdateRequest(
    @JsonProperty("guest_count") int guestCount
) {}
