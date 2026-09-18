package com.lan.app.repository;

import java.util.List;
import java.util.UUID;

import com.lan.app.domain.model.Event;

public interface EventRepository {
    List<Event> list();
    Event get(UUID externalId);
    Event getByRowId(int rowId);

    /** Evicts the cached {@link #list()} result so a capacity change (registration, cancellation,
     * guest count update) is reflected immediately instead of after the cache's TTL. */
    void invalidateListCache();
}
