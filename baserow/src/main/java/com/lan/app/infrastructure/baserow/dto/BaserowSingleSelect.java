package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BaserowSingleSelect(
    int id,
    String value,
    String color
) {
}
