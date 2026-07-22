package com.lan.app.infrastructure.baserow.repository;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.lan.app.domain.model.Vacancy;
import com.lan.app.infrastructure.baserow.client.BaserowVacancyClient;
import com.lan.app.infrastructure.baserow.mapper.BaserowVacancyMapper;
import com.lan.app.repository.VacancyRepository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaserowVacancyRepository implements VacancyRepository {

    private final int vacanciesTableId;

    private final BaserowVacancyClient client;
    private final BaserowVacancyMapper mapper;

    BaserowVacancyRepository(
        @ConfigProperty(name = "baserow.careers.vacancies-table-id") int vacanciesTableId,
        @RestClient BaserowVacancyClient client,
        BaserowVacancyMapper mapper
    ) {
        this.vacanciesTableId = vacanciesTableId;
        this.client = client;
        this.mapper = mapper;
    }

    public List<Vacancy> list() {
        var row = client.list(vacanciesTableId);
        return row.results().stream().map(mapper::toDomain).toList();
    }

    public Vacancy get(UUID externalId) {
        var row = client.findUniqueByExternalId(vacanciesTableId, externalId);
        return mapper.toDomain(row);
    }
}
