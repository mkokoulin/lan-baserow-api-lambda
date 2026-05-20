package com.lan.app.service;

import java.util.List;
import java.util.UUID;

import com.lan.app.domain.model.CoworkingGuestTariff;
import com.lan.app.repository.CoworkingGuestTariffRepository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CoworkingGuestTariffService {

    CoworkingGuestTariffRepository repo;

    public CoworkingGuestTariffService(CoworkingGuestTariffRepository repo) {
        this.repo = repo;
    }

    public List<CoworkingGuestTariff> list() {
        return repo.list();
    }

    public CoworkingGuestTariff get(UUID externalId) {
        return repo.get(externalId);
    }
}
