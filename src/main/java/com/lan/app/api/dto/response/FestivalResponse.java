package com.lan.app.api.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FestivalResponse(
    UUID id,
    String name,
    String description,
    List<UUID> eventsIds,
    Instant dateStart,
    Instant dateEnd,
    Boolean isVisible,
    Boolean isPin
) {
}
