package com.lan.app.service;

import java.util.List;
import java.util.UUID;

import com.lan.app.domain.model.Initiative;
import com.lan.app.repository.InitiativeRepository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InitiativeService {

    InitiativeRepository repo;

    public InitiativeService(InitiativeRepository repo) {
        this.repo = repo;
    }

    public List<Initiative> list() {
        return repo.list();
    }

    public Initiative get(UUID externalId) {
        return repo.get(externalId);
    }
}
