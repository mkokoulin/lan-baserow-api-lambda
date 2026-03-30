package com.lan.app.session;

import java.util.Optional;

public interface SessionRepository {
    Optional<Session> findByUserId(Long userId);
    void save(Session session);
}
