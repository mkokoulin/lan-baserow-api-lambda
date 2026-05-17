package com.lan.app.infrastructure.baserow.repository;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.lan.app.domain.model.Festivale;
import com.lan.app.infrastructure.baserow.client.BaserowFestivaleClient;
import com.lan.app.infrastructure.baserow.mapper.BaserowFestivaleMapper;
import com.lan.app.repository.FestivaleRepository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaserowFestivaleRepository implements FestivaleRepository {

    private final int festivaleTableId;

    private final BaserowFestivaleClient client;
    private final BaserowFestivaleMapper mapper;

    BaserowFestivaleRepository(
        @ConfigProperty(name = "baserow.festivale.festivale-table-id") int festivaleTableId,
        @RestClient BaserowFestivaleClient festivaleClient,
        BaserowFestivaleMapper mapper
    ) {
        this.festivaleTableId = festivaleTableId;
        this.client = festivaleClient;
        this.mapper = mapper;
    }

     public List<Festivale> list() {
        var row = client.list(festivaleTableId);
        return row.results().stream().map(mapper::toDomain).toList();
    }

    public Festivale get(UUID externalId) {
        var row = client.findUniqueByExternalId(festivaleTableId, externalId);
        return mapper.toDomain(row);
    } 
}
