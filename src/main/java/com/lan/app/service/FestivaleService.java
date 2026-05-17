package com.lan.app.service;

import java.util.List;
import java.util.UUID;

import com.lan.app.domain.model.Festivale;
import com.lan.app.repository.FestivaleRepository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FestivaleService {

    FestivaleRepository repo;

    public FestivaleService(FestivaleRepository repo) {
        this.repo = repo;
    }

    public List<Festivale> list() {
        return repo.list();
    }

    public Festivale get(UUID externalId) {
        return repo.get(externalId);
    }
    
}
