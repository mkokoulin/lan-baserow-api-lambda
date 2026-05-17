package com.lan.app.domain.model;

import java.time.Instant;

public record Festivale(
    Id id,
    String name,
    String description,
    Instant dateStart,
    Instant dateEnd,
    Boolean isVisible,
    Boolean isPin
) {}
