package com.lan.app.infrastructure.baserow.mapper;

import java.util.UUID;

import com.lan.app.domain.model.CoworkingGuestTariff;
import com.lan.app.infrastructure.baserow.dto.BaserowCoworkingGuestTariffRow;
import com.lan.app.infrastructure.baserow.exception.BaserowDataIntegrityException;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaserowCoworkingGuestTariffMapper {

    public CoworkingGuestTariff toDomain(BaserowCoworkingGuestTariffRow guestTariff) {
        if (guestTariff.tariffId().isEmpty() || guestTariff.guestId().isEmpty()) {
            throw new BaserowDataIntegrityException("CoworkingGuestTariff", guestTariff.externalId());
        }
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
