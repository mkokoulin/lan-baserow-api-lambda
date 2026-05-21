package com.lan.app.infrastructure.baserow.mapper;

import com.lan.app.domain.model.CoworkingTariff;
import com.lan.app.domain.model.CoworkingTariffType;
import com.lan.app.infrastructure.baserow.dto.BaserowCoworkingTariffRow;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaserowCoworkingTariffMapper {

    public CoworkingTariff toDomain(BaserowCoworkingTariffRow row) {
        return new CoworkingTariff(
            row.externalId(),
            row.name(),
            row.price(),
            row.meetingRoom() != null ? row.meetingRoom().value() : null,
            row.fixedDesc(),
            row.filterCoffeeOrTea(),
            row.printoutScan(),
            row.luggageStorage(),
            CoworkingTariffType.fromBaserow(row.type())
        );
    }
}
