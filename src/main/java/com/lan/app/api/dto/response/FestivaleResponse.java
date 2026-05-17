package com.lan.app.api.dto.response;

import java.util.UUID;

public record FestivaleResponse(
    UUID id,
    String name,
    String description
) {
}
