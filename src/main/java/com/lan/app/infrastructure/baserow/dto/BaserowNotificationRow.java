package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BaserowNotificationRow(
    @NotNull @JsonProperty("id") Integer id,
    @NotNull @JsonProperty("external_id") UUID externalId,
    @JsonProperty("offset_days") Integer offsetDays,
    @JsonProperty("send_time") Double sendTimeSeconds,
    @JsonProperty("message_en") String messageEn,
    @JsonProperty("message_ru") String messageRu,
    @JsonProperty("comment") String comment,
    @JsonProperty("active") boolean active
) {}
