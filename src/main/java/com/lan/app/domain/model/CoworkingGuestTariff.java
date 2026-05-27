package com.lan.app.domain.model;

import java.util.UUID;

public record CoworkingGuestTariff(
    UUID id,
    UUID tariffId,
    UUID guestId,
    Integer daysUsed,
    GuestTariffStatus status
) {
}
