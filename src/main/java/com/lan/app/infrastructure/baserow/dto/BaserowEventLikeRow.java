package com.lan.app.infrastructure.baserow.dto;

import com.baserow.dto.BaserowLinkToTable;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BaserowEventLikeRow(
    @NotNull @JsonProperty("id") Integer id,
    @Nullable @JsonProperty("event_id") List<BaserowLinkToTable> event,
    @NotNull @NotBlank @JsonProperty("anon_id") String anonId,
    @Nullable @JsonProperty("created_at") String createdAt
) {
}
