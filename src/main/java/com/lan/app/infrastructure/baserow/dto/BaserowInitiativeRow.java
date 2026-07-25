package com.lan.app.infrastructure.baserow.dto;

import com.baserow.dto.BaserowFile;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BaserowInitiativeRow(
    @NotNull @JsonProperty("id") Integer id,
    @NotNull @JsonProperty("external_id") UUID externalId,
    @NotBlank @JsonProperty("title") String title,
    @NotBlank @JsonProperty("description") String description,
    @Nullable @JsonProperty("image") List<BaserowFile> image,
    @Nullable @JsonProperty("href") String href,
    @NotNull @JsonProperty("is_visible") Boolean isVisible
) {}
