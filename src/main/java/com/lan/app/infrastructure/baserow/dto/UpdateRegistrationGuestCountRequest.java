package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateRegistrationGuestCountRequest(
    @JsonProperty("guest_count") int guestCount
) {}
