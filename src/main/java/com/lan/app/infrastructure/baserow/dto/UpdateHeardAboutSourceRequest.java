package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateHeardAboutSourceRequest(
    @JsonProperty("heard_about_source") String heardAboutSource,
    @JsonProperty("heard_about_comment") String heardAboutComment,
    @JsonProperty("heard_about_survey_sent") Boolean heardAboutSurveySent
) {}
