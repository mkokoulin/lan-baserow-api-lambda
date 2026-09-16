package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BaserowEventLikeRow(
    @NotNull @JsonProperty("id") Integer id,
    @NotNull @JsonProperty("event_external_id") UUID eventExternalId,
    @NotNull @NotBlank @JsonProperty("anon_id") String anonId,
    @Nullable @JsonProperty("created_at") String createdAt
) {
}
