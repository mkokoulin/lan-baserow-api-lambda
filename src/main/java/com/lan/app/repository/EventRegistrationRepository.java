package com.lan.app.repository;

import com.lan.app.domain.model.EventRegistration;
import com.lan.app.domain.model.Id;

public interface EventRegistrationRepository {
    EventRegistration create(Id eventId, Id guestId, int guestCount, String comment, String source);
}
