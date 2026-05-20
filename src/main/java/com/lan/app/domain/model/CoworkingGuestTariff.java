package com.lan.app.domain.model;

import java.time.Instant;
import java.util.UUID;

public record CoworkingGuestTariff(
    UUID id,
    UUID tariffId,
    UUID guestId,
    Instant dateStart,
    Instant dateEnd,
    Integer daysUsed
) {
}
