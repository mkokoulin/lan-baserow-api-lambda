package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateBirthdayWishRowRequest(
    @NotNull @NotBlank @JsonProperty("author_name") String authorName,
    @JsonProperty("telegram_user_id") String telegramUserId,
    @JsonProperty("text") String text
) {
}
