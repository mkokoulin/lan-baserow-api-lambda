package com.lan.app.backend;

import com.lan.app.session.Session;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class BackendClient {

    private final Map<Long, Session> storage = new ConcurrentHashMap<>();

    public Optional<Session> getSession(Long userId) {
        return Optional.ofNullable(storage.get(userId));
    }

    public Session saveSession(Session session) {
        session.setUpdatedAt(OffsetDateTime.now());
        storage.put(session.getUserId(), session);
        return session;
    }
}
