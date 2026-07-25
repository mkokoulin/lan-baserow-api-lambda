package com.lan.app.infrastructure.baserow.repository;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.lan.app.domain.model.Initiative;
import com.lan.app.infrastructure.baserow.client.BaserowInitiativeClient;
import com.lan.app.infrastructure.baserow.mapper.BaserowInitiativeMapper;
import com.lan.app.repository.InitiativeRepository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaserowInitiativeRepository implements InitiativeRepository {

    private final int initiativesTableId;

    private final BaserowInitiativeClient client;
    private final BaserowInitiativeMapper mapper;

    BaserowInitiativeRepository(
        @ConfigProperty(name = "baserow.initiatives.initiatives-table-id") int initiativesTableId,
        @RestClient BaserowInitiativeClient client,
        BaserowInitiativeMapper mapper
    ) {
        this.initiativesTableId = initiativesTableId;
        this.client = client;
        this.mapper = mapper;
    }

    public List<Initiative> list() {
        var row = client.list(initiativesTableId);
        return row.results().stream().map(mapper::toDomain).toList();
    }

    public Initiative get(UUID externalId) {
        var row = client.findUniqueByExternalId(initiativesTableId, externalId);
        return mapper.toDomain(row);
    }
}
