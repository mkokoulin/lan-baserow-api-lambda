package com.lan.app.infrastructure.baserow.dto;
import com.baserow.dto.BaserowSingleSelect;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BaserowGuestRow(
    @JsonProperty("id") Integer id,
    @JsonProperty("external_id") UUID externalId,
    @JsonProperty("first_name") String firstName,
    @JsonProperty("last_name") String lastName,
    @JsonProperty("phone") String phone,
    @JsonProperty("telegram") String telegram,
    @JsonProperty("telegram_chat_id") Long telegramChatId,
    @JsonProperty("source") BaserowSingleSelect source,
    @JsonProperty("digest_subscribed") Boolean digestSubscribed,
    @JsonProperty("created_at") String createdAt,
    @JsonProperty("heard_about_source") BaserowSingleSelect heardAboutSource,
    @JsonProperty("heard_about_comment") String heardAboutComment,
    @JsonProperty("heard_about_survey_sent") Boolean heardAboutSurveySent
) {}
