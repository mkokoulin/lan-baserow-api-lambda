package com.lan.app.service;

import com.lan.app.domain.model.EventRegistration;
import com.lan.app.domain.model.EventRegistrationItem;
import com.lan.app.repository.EventGuestRepository;
import com.lan.app.repository.EventRegistrationRepository;
import com.lan.app.repository.EventRepository;
import com.lan.app.service.command.CreateEventRegistrationCommand;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class EventRegistrationService {

    private static final Logger log = Logger.getLogger(EventRegistrationService.class);

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

    public List<EventRegistrationItem> findByChatId(Long chatId) {
        return registrationRepo.findByChatId(chatId);
    }

    public void storeTelegramChatId(UUID regExternalId, Long chatId) {
        try {
            registrationRepo.storeTelegramChatId(regExternalId, chatId);
        } catch (Exception e) {
            log.warnf("Failed to store chatId=%d for reg=%s: %s", chatId, regExternalId, e.getMessage());
        }
    }
}
