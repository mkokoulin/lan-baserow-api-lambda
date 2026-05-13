package com.lan.app.repository;

import com.lan.app.domain.model.EventRegistration;
import com.lan.app.domain.model.EventRegistrationItem;
import com.lan.app.domain.model.Id;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRegistrationRepository {
    EventRegistration create(Id eventId, Id guestId, int guestCount, String comment, String source);
    Optional<Integer> getGuestRowIdByExternalId(UUID regExternalId);
    List<EventRegistrationItem> findByGuestRowId(int guestRowId);
}
