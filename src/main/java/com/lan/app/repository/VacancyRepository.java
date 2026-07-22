package com.lan.app.repository;

import java.util.List;
import java.util.UUID;

import com.lan.app.domain.model.Vacancy;

public interface VacancyRepository {
    List<Vacancy> list();
    Vacancy get(UUID externalId);
}
