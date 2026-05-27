package com.lan.app.api.mapper;

import com.lan.app.api.dto.response.CoworkingGuestTariffResponse;
import com.lan.app.domain.model.CoworkingGuestTariff;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApiCoworkingGuestTariffMapper {

    public CoworkingGuestTariffResponse toResponse(CoworkingGuestTariff guestTariff) {
        return new CoworkingGuestTariffResponse(
            guestTariff.id(),
            guestTariff.tariffId(),
            guestTariff.guestId(),
            guestTariff.daysUsed(),
            guestTariff.status()
        );
    }
}