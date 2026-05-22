package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BaserowReviewRow(
    @NotNull @JsonProperty("id") Integer id,
    @NotNull @NotBlank @JsonProperty("external_id") UUID externalId,
    @NotNull @NotBlank @JsonProperty("author_name") String authorName,
    @NotNull @JsonProperty("rating") Integer rating,
    @Nullable @JsonProperty("text") String text,
    @Nullable @JsonProperty("created_at") String createdAt,
    @Nullable @JsonProperty("is_published") Boolean isPublished
) {
}
