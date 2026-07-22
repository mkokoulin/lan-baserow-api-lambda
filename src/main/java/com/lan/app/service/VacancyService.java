package com.lan.app.service;

import java.util.List;
import java.util.UUID;

import com.lan.app.domain.model.Vacancy;
import com.lan.app.repository.VacancyRepository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class VacancyService {

    VacancyRepository repo;

    public VacancyService(VacancyRepository repo) {
        this.repo = repo;
    }

    public List<Vacancy> list() {
        return repo.list();
    }

    public Vacancy get(UUID externalId) {
        return repo.get(externalId);
    }
}
