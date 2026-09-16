package com.lan.app.repository;

import java.util.Map;
import java.util.UUID;

public interface EventLikeRepository {
    Map<UUID, Long> countsByEvent();
    long count(UUID eventExternalId);
    long like(UUID eventExternalId, String anonId);
    long unlike(UUID eventExternalId, String anonId);
}
