package com.lan.app.service;

import com.lan.app.domain.model.EventRegistration;
import com.lan.app.repository.EventGuestRepository;
import com.lan.app.repository.EventRegistrationRepository;
import com.lan.app.repository.EventRepository;
import com.lan.app.service.command.CreateEventRegistrationCommand;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EventRegistrationService {

    private final EventRepository eventRepo;
    private final EventGuestRepository guestRepo;
    private final EventRegistrationRepository registrationRepo;

    public EventRegistrationService(
        EventRepository eventRepo,
        EventGuestRepository guestRepo,
        EventRegistrationRepository registrationRepo
    ) {
        this.eventRepo = eventRepo;
        this.guestRepo = guestRepo;
        this.registrationRepo = registrationRepo;
    }

    public EventRegistration create(CreateEventRegistrationCommand cmd) {
        var event = eventRepo.get(cmd.eventId());
        var guest = guestRepo.get(cmd.guestId());

        return registrationRepo.create(
            event.id(),
            guest.id(),
            cmd.guestCount(),
            cmd.comment(),
            cmd.source()
        );
    }
}
