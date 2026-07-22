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
}
