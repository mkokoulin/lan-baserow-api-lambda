package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateReviewRowRequest(
    @NotNull @NotBlank @JsonProperty("author_name") String authorName,
    @NotNull @JsonProperty("rating") Integer rating,
    @JsonProperty("text") String text
) {
}
