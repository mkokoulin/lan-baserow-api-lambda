package com.lan.app.api.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HeardAboutSourceAnswerRequest(
    @JsonProperty("source") String source,
    @JsonProperty("comment") String comment
) {}
