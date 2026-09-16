package com.lan.app.infrastructure.baserow.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.lan.app.infrastructure.baserow.client.BaserowEventClient;
import com.lan.app.infrastructure.baserow.client.BaserowEventLikeClient;
import com.lan.app.infrastructure.baserow.dto.CreateEventLikeRowRequest;
import com.lan.app.repository.EventLikeRepository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaserowEventLikeRepository implements EventLikeRepository {

    private final int likesTableId;
    private final int eventsTableId;
    private final BaserowEventLikeClient client;
    private final BaserowEventClient eventClient;

    BaserowEventLikeRepository(
        @ConfigProperty(name = "baserow.events.likes-table-id") int likesTableId,
        @ConfigProperty(name = "baserow.events.events-table-id") int eventsTableId,
        @RestClient BaserowEventLikeClient client,
        @RestClient BaserowEventClient eventClient
    ) {
        this.likesTableId = likesTableId;
        this.eventsTableId = eventsTableId;
        this.client = client;
        this.eventClient = eventClient;
    }

    @Override
    public Map<Integer, Long> countsByEvent() {
        var counts = new HashMap<Integer, Long>();
        for (var row : client.listAll(likesTableId).results()) {
            if (row.event() == null || row.event().isEmpty()) continue;
            counts.merge(row.event().getFirst().id(), 1L, Long::sum);
        }
        return counts;
    }

    @Override
    public long count(UUID eventExternalId) {
        return count(resolveEventRowId(eventExternalId));
    }

    @Override
    public long like(UUID eventExternalId, String anonId) {
        int eventRowId = resolveEventRowId(eventExternalId);
        var existing = client.findByEventAndAnon(likesTableId, eventRowId, anonId);
        if (existing.results().isEmpty()) {
            client.create(likesTableId, new CreateEventLikeRowRequest(List.of(eventRowId), anonId));
        }
        return count(eventRowId);
    }

    @Override
    public long unlike(UUID eventExternalId, String anonId) {
        int eventRowId = resolveEventRowId(eventExternalId);
        var existing = client.findByEventAndAnon(likesTableId, eventRowId, anonId);
        for (var row : existing.results()) {
            client.delete(likesTableId, row.id());
        }
        return count(eventRowId);
    }

    private int resolveEventRowId(UUID eventExternalId) {
        return eventClient.findUniqueByExternalId(eventsTableId, eventExternalId).id();
    }

    private long count(int eventRowId) {
        return client.findByEvent(likesTableId, eventRowId).count();
    }
}
