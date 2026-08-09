package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateReviewRowRequest(
    @NotNull @NotBlank @JsonProperty("author_name") String authorName,
    @NotNull @JsonProperty("rating") Integer rating,
    @JsonProperty("text") String text,
    @JsonProperty("event_id") List<Integer> eventId,
    @JsonProperty("guest_id") List<Integer> guestId,
    @JsonProperty("registration_id") List<Integer> registrationId
) {
}
