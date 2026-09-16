package com.lan.app.service;

import java.util.Map;
import java.util.UUID;

import com.lan.app.repository.EventLikeRepository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EventLikeService {

    private final EventLikeRepository repo;

    public EventLikeService(EventLikeRepository repo) {
        this.repo = repo;
    }

    /** Keyed by the internal Baserow row id of the event, not its external UUID. */
    public Map<Integer, Long> countsByEvent() {
        return repo.countsByEvent();
    }

    public long count(UUID eventExternalId) {
        return repo.count(eventExternalId);
    }

    public long like(UUID eventExternalId, String anonId) {
        return repo.like(eventExternalId, anonId);
    }

    public long unlike(UUID eventExternalId, String anonId) {
        return repo.unlike(eventExternalId, anonId);
    }
}
