package com.lan.app.infrastructure.baserow.repository;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.lan.app.domain.model.CoworkingNew;
import com.lan.app.infrastructure.baserow.client.BaserowCoworkingNewClient;
import com.lan.app.infrastructure.baserow.mapper.BaserowCoworkingNewMapper;
import com.lan.app.repository.CoworkingNewRepository;

public class BaserowCoworkingNewRepository implements CoworkingNewRepository {
    
    private final int tableId;

    private final BaserowCoworkingNewClient client;
    private final BaserowCoworkingNewMapper mapper;

    BaserowCoworkingNewRepository(
        @ConfigProperty(name = "baserow.coworking.news-table-id") int tableId,
        @RestClient BaserowCoworkingNewClient client,
        BaserowCoworkingNewMapper mapper
    ) {
       this.tableId = tableId;
       this.client = client;
       this.mapper = mapper;
    }

    public List<CoworkingNew> list() {
        var row = client.list(tableId);
        return row.results().stream().map(mapper::toDomain).toList();
    }

    public CoworkingNew get(UUID externalId) {
        var row = client.findUniqueByExternalId(tableId, externalId);
        return mapper.toDomain(row);
    }
}
