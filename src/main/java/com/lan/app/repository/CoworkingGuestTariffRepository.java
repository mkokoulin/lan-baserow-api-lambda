package com.lan.app.repository;

import java.util.List;
import java.util.UUID;

import com.lan.app.domain.model.CoworkingGuestTariff;

public interface CoworkingGuestTariffRepository {
    List<CoworkingGuestTariff> list();
    CoworkingGuestTariff get(UUID externalId);
    CoworkingGuestTariff deductDay(UUID externalId);
}
