package com.lan.app.infrastructure.baserow.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BaserowFaqRow(
    @NotNull @JsonProperty("id") Integer id,
    @NotNull @JsonProperty("external_id") UUID externalId,
    @NotBlank @JsonProperty("question_en") String questionEn,
    @NotBlank @JsonProperty("question_ru") String questionRu,
    @NotBlank @JsonProperty("answer_en") String answerEn,
    @NotBlank @JsonProperty("answer_ru") String answerRu,
    @Nullable @JsonProperty("category_en") String categoryEn,
    @Nullable @JsonProperty("category_ru") String categoryRu,
    @Nullable @JsonProperty("position") Integer position,
    @NotNull @JsonProperty("is_visible") Boolean isVisible
) {}
