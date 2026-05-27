package com.lan.app.api.dto.response;

import java.util.UUID;

import com.lan.app.domain.model.GuestTariffStatus;

public record CoworkingGuestTariffResponse(
    UUID id,
    UUID tariffId,
    UUID guestId,
    Integer daysUsed,
    GuestTariffStatus status
){}