package com.lan.app.service;

import com.lan.app.repository.EventRegistrationRepository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EventCapacityService {

    private final EventRegistrationRepository registrationRepo;

    public EventCapacityService(EventRegistrationRepository registrationRepo) {
        this.registrationRepo = registrationRepo;
    }

    public int registeredGuestCount(int eventRowId) {
        return registrationRepo.countGuests(eventRowId);
    }

    public boolean isSoldOut(Integer maxCapacity, int eventRowId) {
        if (maxCapacity == null) return false;
        return maxCapacity - registeredGuestCount(eventRowId) <= 0;
    }

    /**
     * Remaining seats for the event, or {@code null} when the event is uncapped.
     * Never negative — overselling (if it ever happens) is reported as zero.
     */
    public Integer remainingCapacity(Integer maxCapacity, int eventRowId) {
        if (maxCapacity == null) return null;
        return Math.max(0, maxCapacity - registeredGuestCount(eventRowId));
    }
}
