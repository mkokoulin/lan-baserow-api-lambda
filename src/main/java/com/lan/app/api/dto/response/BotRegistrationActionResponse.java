package com.lan.app.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record BotRegistrationActionResponse(
    @JsonProperty("event_name") String eventName,
    @JsonProperty("date_start") Instant dateStart,
    @JsonProperty("previous_guest_count") int previousGuestCount,
    @JsonProperty("guest_count") int guestCount,
    @JsonProperty("guest_first_name") String guestFirstName,
    @JsonProperty("guest_last_name") String guestLastName,
    @JsonProperty("guest_phone") String guestPhone,
    @JsonProperty("guest_telegram") String guestTelegram
) {}
