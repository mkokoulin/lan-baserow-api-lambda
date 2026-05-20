package com.lan.app.infrastructure.baserow.mapper;

import java.util.UUID;

import com.lan.app.domain.model.CoworkingGuestTariff;
import com.lan.app.infrastructure.baserow.dto.BaserowCoworkingGuestTariffRow;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaserowCoworkingGuestTariffMapper {

    public CoworkingGuestTariff toDomain(BaserowCoworkingGuestTariffRow guestTariff) {
        return new CoworkingGuestTariff(
            guestTariff.externalId(),
            UUID.fromString(guestTariff.tariffId().getFirst().value()),
            UUID.fromString(guestTariff.guestId().getFirst().value()),
            guestTariff.dateStart(),
            guestTariff.dateEnd(),
            guestTariff.daysUsed()
        );
    }
}
