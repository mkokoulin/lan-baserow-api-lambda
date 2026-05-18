package com.lan.app.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Festival(
    Id id,
    String name,
    String description,
    List<UUID> eventsIds,
    Instant dateStart,
    Instant dateEnd,
    Boolean isVisible,
    Boolean isPin
) {}
