package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record BaserowListResponse<T>(
        @JsonProperty("count") int count,
        @JsonProperty("next") String next,
        @JsonProperty("previous") String previous,
        @JsonProperty("results") List<T> results
) {
}
