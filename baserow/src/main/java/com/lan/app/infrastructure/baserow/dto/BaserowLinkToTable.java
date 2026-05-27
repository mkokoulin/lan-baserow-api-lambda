package com.lan.app.infrastructure.baserow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BaserowLinkToTable(
    @NotNull Integer id,
    @NotNull @NotBlank String value
) {
}
