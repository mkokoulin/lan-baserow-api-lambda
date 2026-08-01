package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record UpdateRegistrationGuestLinkRequest(
    @JsonProperty("guest_id") List<Integer> guestId
) {}
