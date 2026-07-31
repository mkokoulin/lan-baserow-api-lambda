package com.lan.app.repository;

import com.lan.app.domain.model.EventRegistration;
import com.lan.app.domain.model.EventRegistrationItem;
import com.lan.app.domain.model.Id;
import com.lan.app.domain.model.RegistrationActionResult;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface EventRegistrationRepository {
    EventRegistration create(Id eventId, Id guestId, int guestCount, String comment, String source);
    Optional<Integer> getGuestRowIdByExternalId(UUID regExternalId);
    Optional<Integer> getEventRowIdByExternalId(UUID regExternalId);
    List<EventRegistrationItem> findByGuestRowId(int guestRowId);
    Optional<Long> markPaid(UUID externalId);
    Optional<EventRegistrationItem> findByExternalId(UUID regExternalId);
    /** Guest counts exclude cancelled registrations. */
    int countGuests(int eventRowId);
    /** Guest counts for every event, keyed by event row id, in a single Baserow round trip. Excludes cancelled registrations. */
    Map<Integer, Integer> countGuestsByEvent();
    Optional<RegistrationActionResult> cancel(UUID externalId);
    Optional<RegistrationActionResult> updateGuestCount(UUID externalId, int newGuestCount);
}
