package com.lan.app.infrastructure.baserow.repository;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.lan.app.domain.model.Festival;
import com.lan.app.infrastructure.baserow.client.BaserowEventsFestivalClient;
import com.lan.app.infrastructure.baserow.mapper.BaserowEventsFestivalMapper;
import com.lan.app.repository.EventsFestivalRepository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaserowEventsFestivalRepository implements EventsFestivalRepository {

    private final int festivaleTableId;

    private final BaserowEventsFestivalClient client;
    private final BaserowEventsFestivalMapper mapper;

    BaserowEventsFestivalRepository(
        @ConfigProperty(name = "baserow.events.festivals-table-id") int festivaleTableId,
        @RestClient BaserowEventsFestivalClient festivaleClient,
        BaserowEventsFestivalMapper mapper
    ) {
        this.festivaleTableId = festivaleTableId;
        this.client = festivaleClient;
        this.mapper = mapper;
    }

     public List<Festival> list() {
        var row = client.list(festivaleTableId);
        return row.results().stream().map(mapper::toDomain).toList();
    }

    public Festival get(UUID externalId) {
        var row = client.findUniqueByExternalId(festivaleTableId, externalId);
        return mapper.toDomain(row);
    } 
}
