package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateEventLikeRowRequest(
    @NotNull @JsonProperty("event_external_id") UUID eventExternalId,
    @NotNull @NotBlank @JsonProperty("anon_id") String anonId
) {
}
