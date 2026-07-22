package com.lan.app.repository;

import com.lan.app.domain.model.CoworkingActiveTariff;
import com.lan.app.domain.model.CoworkingActiveTariffListItem;

import java.util.List;
import java.util.UUID;

public interface CoworkingActiveTariffRepository {
    List<CoworkingActiveTariffListItem> list();
    CoworkingActiveTariff get(UUID externalId);
}
