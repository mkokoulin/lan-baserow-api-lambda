package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateEventRegistrationRowRequest(
    @NotNull @NotEmpty @JsonProperty("event_id") List<Integer> eventId,
    @NotNull @NotEmpty @JsonProperty("guest_id") List<Integer> guestId,
    @Min(1) @JsonProperty("guest_count") int guestCount,
    @JsonProperty("guest_comment") String guestComment,
    @JsonProperty("source") String source,
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty("external_id") UUID externalId
) {
}
