package com.lan.app.api.mapper;

import com.lan.app.api.dto.response.CoworkingTariffResponse;
import com.lan.app.domain.model.CoworkingTariff;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApiCoworkingTariffMapper {

    public CoworkingTariffResponse toResponse(CoworkingTariff r) {
        return new CoworkingTariffResponse(
            r.id(),
            r.name(),
            r.price(),
            r.meetingRoom(),
            r.fixedDesk(),
            r.filterCoffeeAndTea(),
            r.printoutScan(),
            r.luggageStorage(),
            r.type()
        );
    }
}
