package com.lan.app.infrastructure.baserow.repository;

import com.lan.app.domain.model.CoworkingTariff;
import com.lan.app.infrastructure.baserow.client.BaserowCoworkingTariffClient;
import com.lan.app.infrastructure.baserow.mapper.BaserowCoworkingTariffMapper;
import com.lan.app.repository.CoworkingTariffRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class BaserowCoworkingTariffRepository extends RetryingBaserowRepository implements CoworkingTariffRepository {

    private final int coworkingTariffTableId;

    private final BaserowCoworkingTariffClient coworkingTariffClient;
    private final BaserowCoworkingTariffMapper mapper;

    BaserowCoworkingTariffRepository(
        @ConfigProperty(name = "baserow.coworking.tariffs-table-id") int coworkingTariffTableId,
        @RestClient BaserowCoworkingTariffClient coworkingTariffClient,
        BaserowCoworkingTariffMapper mapper
    ) {
       this.coworkingTariffTableId = coworkingTariffTableId;
       this.coworkingTariffClient = coworkingTariffClient;
       this.mapper = mapper;
    }

    public List<CoworkingTariff> list() {
        var row = executeIdempotent(() -> coworkingTariffClient.list(coworkingTariffTableId));
        return row.results().stream().map(mapper::toDomain).toList();
    }

    public CoworkingTariff get(UUID externalId) {
        var row = executeIdempotent(() -> coworkingTariffClient.findUniqueByExternalId(coworkingTariffTableId, externalId));
        return mapper.toDomain(row);
    }
}
