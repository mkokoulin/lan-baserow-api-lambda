package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateSurveySentRequest(
    @JsonProperty("survey_sent") Boolean surveySent
) {}
