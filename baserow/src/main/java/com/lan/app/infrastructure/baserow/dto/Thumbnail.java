package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Thumbnail(String url, Integer width, Integer height) {}