package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record BaserowRegistrationRow(
    @NotNull @JsonProperty("id") Integer id,
    @NotNull @JsonProperty("external_id") UUID externalId,

    @NotNull @NotEmpty @Valid
    @JsonProperty("event_id") List<@Valid BaserowLinkToTable> eventId,

    @NotNull @NotEmpty @Valid
    @JsonProperty("guest_id") List<@Valid BaserowLinkToTable> guestId,

    @JsonProperty("created_at") String createdAt,
    @NotNull @JsonProperty("guest_count") Integer guestCount,
    @JsonProperty("guest_comment") String guestComment,
    @JsonProperty("source") BaserowSingleSelect source
) {
}
