package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record BaserowSupportComplaintRow(
    @NotNull @JsonProperty("id") Integer id,
    @JsonProperty("name") String name,
    @JsonProperty("phone") String phone,
    @JsonProperty("telegram") String telegram,
    @JsonProperty("topic_custom") String topicCustom,
    @JsonProperty("comment") String comment
) {
}
