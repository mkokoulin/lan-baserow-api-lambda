package com.lan.app.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    name = "CreateEventGuestRequest",
    description = "Payload for creating a new event guest"
)
public record CreateEventGuestRequest(

    @Schema(description = "Guest's first name", required = true)
    @NotNull @NotBlank @JsonProperty("firstName") String firstName,

    @Schema(description = "Guest's last name (optional)", nullable = true)
    @JsonProperty("lastName") String lastName,

    @Schema(description = "Guest's phone number", required = true)
    @NotNull @NotBlank @JsonProperty("phone") String phone,

    @Schema(description = "Guest's Telegram username (with @)", nullable = true)
    @JsonProperty("telegram") String telegram,

    @Schema(description = "Source channel, e.g. 'website'", nullable = true)
    @JsonProperty("source") String source
) {
}
