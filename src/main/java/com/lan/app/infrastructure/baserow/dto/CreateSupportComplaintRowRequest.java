package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateSupportComplaintRowRequest(
    @NotNull @NotBlank @JsonProperty("name") String name,
    @NotNull @JsonProperty("phone") String phone,
    @JsonProperty("telegram") String telegram,
    @NotNull @JsonProperty("topic") String topic,
    @JsonProperty("topic_custom") String topicCustom,
    @JsonProperty("comment") String comment,
    @JsonProperty("status") String status
) {
}
