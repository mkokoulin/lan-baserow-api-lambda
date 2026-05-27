package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BaserowCollaborator(int id, String name) {}
