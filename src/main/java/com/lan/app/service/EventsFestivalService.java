package com.lan.app.service;

import java.util.List;
import java.util.UUID;

import com.lan.app.domain.model.Festival;
import com.lan.app.repository.EventsFestivalRepository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EventsFestivalService {

    EventsFestivalRepository repo;

    public EventsFestivalService(EventsFestivalRepository repo) {
        this.repo = repo;
    }

    public List<Festival> list() {
        return repo.list();
    }

    public Festival get(UUID externalId) {
        return repo.get(externalId);
    }
    
}
