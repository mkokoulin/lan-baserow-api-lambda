package com.lan.app.repository;

import com.lan.app.domain.model.EventGuest;

import java.util.UUID;

public interface EventGuestRepository {
    EventGuest get(UUID externalId);
    EventGuest create(String firstName, String lastName, String phone, String telegram, String source);
}
