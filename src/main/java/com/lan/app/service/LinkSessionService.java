package com.lan.app.service;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class LinkSessionService {

    public enum LinkStatus { PENDING, CONFIRMED, REJECTED }

    private final ConcurrentHashMap<UUID, LinkStatus> sessions = new ConcurrentHashMap<>();

    public void init(UUID guestId) {
        sessions.put(guestId, LinkStatus.PENDING);
    }

    public void confirm(UUID guestId) {
        sessions.put(guestId, LinkStatus.CONFIRMED);
    }

    public void reject(UUID guestId) {
        sessions.put(guestId, LinkStatus.REJECTED);
    }

    public LinkStatus getStatus(UUID guestId) {
        return sessions.getOrDefault(guestId, LinkStatus.PENDING);
    }
}
