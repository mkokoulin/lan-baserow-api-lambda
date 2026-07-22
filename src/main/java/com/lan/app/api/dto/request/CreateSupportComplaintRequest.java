package com.lan.app.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateSupportComplaintRequest(
    @NotNull @NotBlank @JsonProperty("name") String name,
    @NotNull @NotBlank @JsonProperty("phone") String phone,
    @JsonProperty("telegram") String telegram,
    @NotNull @NotBlank @JsonProperty("topic") String topic,
    @JsonProperty("topicCustom") String topicCustom,
    @JsonProperty("comment") String comment
) {
}
