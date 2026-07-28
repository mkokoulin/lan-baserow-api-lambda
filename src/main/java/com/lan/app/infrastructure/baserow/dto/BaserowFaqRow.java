package com.lan.app.infrastructure.baserow.dto;

import java.util.List;
import java.util.UUID;

import com.baserow.dto.BaserowLinkToTable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// ignoreUnknown so future Baserow columns added by editors don't break deserialization
// of the rows this record doesn't care about (bit us with blog_post below — declare
// every field we know exists, but don't let an unknown one fail the whole row).
@JsonIgnoreProperties(ignoreUnknown = true)
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
    @NotNull @JsonProperty("is_visible") Boolean isVisible,
    // Optional link to the blog post this FAQ entry belongs to. Empty/absent means
    // it's a general entry (still shown on the standalone /faq page).
    @Nullable @JsonProperty("blog_post") List<BaserowLinkToTable> blogPost
) {}
