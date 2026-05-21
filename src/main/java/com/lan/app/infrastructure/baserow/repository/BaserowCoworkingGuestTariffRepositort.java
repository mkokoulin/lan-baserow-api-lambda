package com.lan.app.infrastructure.baserow.repository;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.lan.app.domain.model.CoworkingGuestTariff;
import com.lan.app.infrastructure.baserow.client.BaserowCoworkingGuestTariffClient;
import com.lan.app.infrastructure.baserow.dto.UpdateGuestTariffDaysUsedRequest;
import com.lan.app.infrastructure.baserow.mapper.BaserowCoworkingGuestTariffMapper;
import com.lan.app.repository.CoworkingGuestTariffRepository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaserowCoworkingGuestTariffRepositort implements CoworkingGuestTariffRepository {

    private final int tableId;

    private final BaserowCoworkingGuestTariffClient client;
    private final BaserowCoworkingGuestTariffMapper mapper;

    BaserowCoworkingGuestTariffRepositort(
        @ConfigProperty(name = "baserow.coworking.guest-tariffs-table-id") int tableId,
        @RestClient BaserowCoworkingGuestTariffClient client,
        BaserowCoworkingGuestTariffMapper mapper
    ) {
       this.tableId = tableId;
       this.client = client;
       this.mapper = mapper;
    }

    public List<CoworkingGuestTariff> list() {
        var row = client.list(tableId);
        return row.results().stream().map(mapper::toDomain).toList();
    }

    public CoworkingGuestTariff get(UUID externalId) {
        var row = client.findUniqueByExternalId(tableId, externalId);
        return mapper.toDomain(row);
    }

    public CoworkingGuestTariff deductDay(UUID externalId) {
        var row = client.findUniqueByExternalId(tableId, externalId);
        int newDaysUsed = (row.daysUsed() != null ? row.daysUsed() : 0) + 1;
        var updated = client.patchDaysUsed(tableId, row.id(), new UpdateGuestTariffDaysUsedRequest(newDaysUsed));
        return mapper.toDomain(updated);
    }
}
