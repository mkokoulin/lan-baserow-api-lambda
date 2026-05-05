package com.lan.app.repository;

import java.util.List;
import java.util.UUID;

import com.lan.app.domain.model.Event;

public interface EventRepository {
    List<Event> list();
    Event get(UUID externalId);
}
