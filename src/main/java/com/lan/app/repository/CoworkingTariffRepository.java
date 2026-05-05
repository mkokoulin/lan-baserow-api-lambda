package com.lan.app.repository;

import java.util.List;
import java.util.UUID;

import com.lan.app.domain.model.CoworkingTariff;

public interface CoworkingTariffRepository {
    List<CoworkingTariff> list();
    CoworkingTariff get(UUID externalId);
}
