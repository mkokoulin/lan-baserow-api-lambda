package com.lan.app.repository;

import java.util.Map;
import java.util.UUID;

public interface EventLikeRepository {
    /** Keyed by the internal Baserow row id of the event (the {@code event_id} link field), not its external UUID. */
    Map<Integer, Long> countsByEvent();
    long count(UUID eventExternalId);
    long like(UUID eventExternalId, String anonId);
    long unlike(UUID eventExternalId, String anonId);
}
