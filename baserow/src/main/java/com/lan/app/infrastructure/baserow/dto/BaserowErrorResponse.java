package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BaserowErrorResponse(String error, String detail) {}
