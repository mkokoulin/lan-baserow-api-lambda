package com.lan.app.api.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CoworkingGuestTariffResponse(
    UUID id,
    UUID tariffId,
    UUID guestId,
    Instant dateStart,
    Instant dateEnd,
    Integer daysUsed
){}