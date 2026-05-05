package com.lan.app.service;

import java.util.List;
import java.util.UUID;

import com.lan.app.domain.model.Event;
import com.lan.app.repository.EventRepository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EventService {
    
    EventRepository repo;

    public EventService(EventRepository repo) {
        this.repo = repo;
    }

    public List<Event> list() {
        return repo.list();
    }

    public Event get(UUID externalId) {
        return repo.get(externalId);
    }
}
