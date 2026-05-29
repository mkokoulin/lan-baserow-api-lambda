package com.lan.app.infrastructure.baserow.dto;
import com.baserow.dto.BaserowFile;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BaserowCoworkingNewRow(
    @NotNull @JsonProperty("id") Integer id,
    @NotNull @JsonProperty("external_id") UUID externalId,
    @NotNull @NotBlank @JsonProperty("title_en") String titleEn,
    @NotNull @NotBlank @JsonProperty("title_ru") String titleRu,
    @NotNull @NotBlank @JsonProperty("body_en") String bodyEn,
    @NotNull @NotBlank @JsonProperty("body_ru") String bodyRu,
    @NotNull @JsonProperty("image") List<BaserowFile> image,
    @JsonProperty("link") String link
) {}
