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
    @JsonProperty("external_registration_url") String externalRegistrationUrl,
    @NotNull @JsonProperty("registration_url") URI registrationUrl,
    @JsonProperty("instagram_url") String instagramUrl,
    @JsonProperty("show_form") boolean showForm,
    @JsonProperty("notifications") List<BaserowLinkToTable> notifications,
    @JsonProperty("comment") String comment,
    @JsonProperty("is_pin") boolean isPin,
    @JsonProperty("requires_prepayment") Boolean requiresPrepayment,
    @JsonProperty("price") java.math.BigDecimal price
) {}
