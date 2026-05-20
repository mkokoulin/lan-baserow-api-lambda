package com.lan.app.service;

import java.util.List;
import java.util.UUID;

import com.lan.app.domain.model.CoworkingNew;
import com.lan.app.repository.CoworkingNewRepository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CoworkingNewService {

    CoworkingNewRepository repo;

    public CoworkingNewService(CoworkingNewRepository repo) {
        this.repo = repo;
    }

    public List<CoworkingNew> list() {
        return repo.list();
    }

    public CoworkingNew get(UUID externalId) {
        return repo.get(externalId);
    }
}