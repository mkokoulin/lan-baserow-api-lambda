package com.lan.app.infrastructure.baserow.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.lan.app.infrastructure.baserow.client.BaserowEventLikeClient;
import com.lan.app.infrastructure.baserow.dto.CreateEventLikeRowRequest;
import com.lan.app.repository.EventLikeRepository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaserowEventLikeRepository implements EventLikeRepository {

    private final int likesTableId;
    private final BaserowEventLikeClient client;

    BaserowEventLikeRepository(
        @ConfigProperty(name = "baserow.events.likes-table-id") int likesTableId,
        @RestClient BaserowEventLikeClient client
    ) {
        this.likesTableId = likesTableId;
        this.client = client;
    }

    @Override
    public Map<UUID, Long> countsByEvent() {
        var counts = new HashMap<UUID, Long>();
        for (var row : client.listAll(likesTableId).results()) {
            counts.merge(row.eventExternalId(), 1L, Long::sum);
        }
        return counts;
    }

    @Override
    public long count(UUID eventExternalId) {
        return client.findByEvent(likesTableId, eventExternalId).count();
    }

    @Override
    public long like(UUID eventExternalId, String anonId) {
        var existing = client.findByEventAndAnon(likesTableId, eventExternalId, anonId);
        if (existing.results().isEmpty()) {
            client.create(likesTableId, new CreateEventLikeRowRequest(eventExternalId, anonId));
        }
        return count(eventExternalId);
    }

    @Override
    public long unlike(UUID eventExternalId, String anonId) {
        var existing = client.findByEventAndAnon(likesTableId, eventExternalId, anonId);
        for (var row : existing.results()) {
            client.delete(likesTableId, row.id());
        }
        return count(eventExternalId);
    }
}
