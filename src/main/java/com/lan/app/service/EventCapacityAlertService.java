package com.lan.app.service;

import com.lan.app.domain.model.Event;
import com.lan.app.domain.model.EventCapacityAlert;
import com.lan.app.repository.EventRepository;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class EventCapacityAlertService {

    private final EventRepository eventRepo;
    private final EventCapacityService capacityService;
    private final EventCapacityAlertStore alertStore;

    public EventCapacityAlertService(
        EventRepository eventRepo,
        EventCapacityService capacityService,
        EventCapacityAlertStore alertStore
    ) {
        this.eventRepo = eventRepo;
        this.capacityService = capacityService;
        this.alertStore = alertStore;
    }

    /**
     * Finds events that just transitioned from sold-out to having room, so the admin can be
     * alerted to work their waitlist. Each returned alert is marked consumed immediately —
     * there is no separate results POST-back, since a missed admin alert is low-stakes
     * compared to guest-facing reminders.
     */
    public List<EventCapacityAlert> findDue() {
        var due = new ArrayList<EventCapacityAlert>();
        for (Event event : eventRepo.list()) {
            if (event.maxCapacity() == null) continue;
            int rowId = event.id().internalId();
            Boolean lastKnown = alertStore.getLastKnown(rowId);
            boolean soldOutNow = event.soldOut();

            if (lastKnown != null && lastKnown && !soldOutNow && !alertStore.alreadyAlerted(rowId)) {
                int registeredCount = capacityService.registeredGuestCount(rowId);
                due.add(new EventCapacityAlert(event.name(), registeredCount, event.maxCapacity()));
                alertStore.markAlerted(rowId);
            }

            alertStore.recordState(rowId, soldOutNow);
        }
        return due;
    }
}
