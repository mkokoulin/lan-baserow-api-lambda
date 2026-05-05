package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BaserowEventRow(
    @NotNull @JsonProperty("id") Integer id,
    @NotNull @JsonProperty("external_id") UUID externalId,
    @NotBlank @JsonProperty("name") String name,
    @NotNull @JsonProperty("date_start") Instant dateStart,
    @NotNull @JsonProperty("date_end") Instant dateEnd,
    @NotBlank @JsonProperty("description") String description,
    @NotNull @JsonProperty("image") List<BaserowFile> image,
    @JsonProperty("external_registration_url") String externalRegistrationUrl,
    @NotNull @JsonProperty("registration_url") URI registrationUrl,
    @JsonProperty("instagram_url") String instagramUrl,
    @JsonProperty("type") BaserowSingleSelect type,
    @JsonProperty("show_form") boolean showForm,
    @JsonProperty("notification_time") List<BaserowSelectOption> notificationTime,
    @JsonProperty("comment") String comment,
    @JsonProperty("show_event") List<BaserowSelectOption> showEvent
) {
    public BaserowEventRow {
        image = image != null ? image : List.of();
        notificationTime = notificationTime != null ? notificationTime : List.of();
        showEvent = showEvent != null ? showEvent : List.of();
    }
}
