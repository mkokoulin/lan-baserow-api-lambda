package com.lan.app.infrastructure.baserow.repository;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.lan.app.domain.model.CoworkingGuestTariff;
import com.lan.app.infrastructure.baserow.client.BaserowCoworkingGuestTariffClient;
import com.lan.app.infrastructure.baserow.client.BaserowCoworkingTariffClient;
import com.lan.app.infrastructure.baserow.client.BaserowGuestClient;
import com.lan.app.infrastructure.baserow.dto.CreateGuestTariffRowRequest;
import com.lan.app.infrastructure.baserow.dto.UpdateGuestTariffDaysUsedRequest;
import com.lan.app.infrastructure.baserow.mapper.BaserowCoworkingGuestTariffMapper;
import com.lan.app.repository.CoworkingGuestTariffRepository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaserowCoworkingGuestTariffRepositort implements CoworkingGuestTariffRepository {

    private final int tableId;
    private final int guestsTableId;
    private final int tariffsTableId;

    private final BaserowCoworkingGuestTariffClient client;
    private final BaserowGuestClient guestClient;
    private final BaserowCoworkingTariffClient tariffClient;
    private final BaserowCoworkingGuestTariffMapper mapper;

    BaserowCoworkingGuestTariffRepositort(
        @ConfigProperty(name = "baserow.coworking.guest-tariffs-table-id") int tableId,
        @ConfigProperty(name = "baserow.guests.guests-table-id") int guestsTableId,
        @ConfigProperty(name = "baserow.coworking.tariffs-table-id") int tariffsTableId,
        @RestClient BaserowCoworkingGuestTariffClient client,
        @RestClient BaserowGuestClient guestClient,
        @RestClient BaserowCoworkingTariffClient tariffClient,
        BaserowCoworkingGuestTariffMapper mapper
    ) {
       this.tableId = tableId;
       this.guestsTableId = guestsTableId;
       this.tariffsTableId = tariffsTableId;
       this.client = client;
       this.guestClient = guestClient;
       this.tariffClient = tariffClient;
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

    public CoworkingGuestTariff create(UUID guestExternalId, UUID tariffExternalId) {
        var guestRow = guestClient.findUniqueByExternalId(guestsTableId, guestExternalId);
        var tariffRow = tariffClient.findUniqueByExternalId(tariffsTableId, tariffExternalId);
        var req = new CreateGuestTariffRowRequest(
            UUID.randomUUID(),
            List.of(guestRow.id()),
            List.of(tariffRow.id())
        );
        var created = client.create(tableId, req);
        return mapper.toDomain(created);
    }

    public CoworkingGuestTariff deductDay(UUID externalId) {
        var row = client.findUniqueByExternalId(tableId, externalId);
        int newDaysUsed = (row.daysUsed() != null ? row.daysUsed() : 0) + 1;
        var updated = client.patchDaysUsed(tableId, row.id(), new UpdateGuestTariffDaysUsedRequest(newDaysUsed));
        return mapper.toDomain(updated);
    }

    @Override
    public List<CoworkingGuestTariff> findByGuestExternalId(UUID guestExternalId) {
        var guestRow = guestClient.findUniqueByExternalId(guestsTableId, guestExternalId);
        return client.findAllByGuestRowId(tableId, guestRow.id()).results().stream()
            .filter(row -> !row.tariffId().isEmpty() && !row.guestId().isEmpty())
            .map(mapper::toDomain)
            .toList();
    }
}
