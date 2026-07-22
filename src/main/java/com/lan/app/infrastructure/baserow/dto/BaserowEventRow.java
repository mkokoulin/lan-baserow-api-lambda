package com.lan.app.infrastructure.baserow.dto;
import com.baserow.dto.BaserowFile;
import com.baserow.dto.BaserowLinkToTable;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.net.URI;
import java.util.List;
import java.util.UUID;

public record BaserowEventRow(
    @NotNull @JsonProperty("id") Integer id,
    @NotNull @JsonProperty("external_id") UUID externalId,
    @NotBlank @JsonProperty("name") String name,
    @JsonProperty("date_start") String dateStart,
    @JsonProperty("date_end") String dateEnd,
    @NotBlank @JsonProperty("description") String description,
    @JsonProperty("external_registration_url") String externalRegistrationUrl,
    @NotNull @JsonProperty("registration_url") URI registrationUrl,
    @JsonProperty("instagram_url") String instagramUrl,
    @JsonProperty("telegram_url") String telegramUrl,
    @JsonProperty("show_form") boolean showForm,
    @JsonProperty("notifications") List<BaserowLinkToTable> notifications,
    @JsonProperty("comment") String comment,
    @JsonProperty("position") Integer position,
    @JsonProperty("show_on_home") boolean showOnHome,
    @JsonProperty("is_visible") Boolean isVisible,
    @JsonProperty("requires_prepayment") Boolean requiresPrepayment,
    @JsonProperty("price") java.math.BigDecimal price,
    @JsonProperty("image") List<BaserowFile> image,
    @JsonProperty("max_capacity") Integer maxCapacity
) {}
