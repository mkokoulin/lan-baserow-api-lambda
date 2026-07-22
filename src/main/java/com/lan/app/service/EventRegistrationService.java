package com.lan.app.service;

import com.lan.app.domain.exception.BusinessConflictException;
import com.lan.app.domain.model.EventRegistration;
import com.lan.app.domain.model.EventRegistrationItem;
import com.lan.app.repository.EventGuestRepository;
import com.lan.app.repository.EventRegistrationRepository;
import com.lan.app.repository.EventRepository;
import com.lan.app.service.command.CreateEventRegistrationCommand;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        if (event.soldOut()) {
            throw new BusinessConflictException(
                "Event is sold out.",
                Map.of("eventId", event.id().externalId().toString())
            );
        }
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
        return guestRepo.findByTelegramChatId(chatId)
                .map(guest -> registrationRepo.findByGuestRowId(guest.id().internalId()))
                .orElse(List.of());
    }

    public List<EventRegistrationItem> findByGuestExternalId(UUID guestExternalId) {
        var guest = guestRepo.get(guestExternalId);
        return registrationRepo.findByGuestRowId(guest.id().internalId());
    }

    public void storeTelegramChatIdForGuest(int guestRowId, Long chatId) {
        try {
            guestRepo.storeTelegramChatId(guestRowId, chatId);
        } catch (Exception e) {
            log.warnf("Failed to store chatId=%d for guestRowId=%d: %s", chatId, guestRowId, e.getMessage());
        }
    }

    public Optional<Long> markPaid(UUID regExternalId) {
        return registrationRepo.markPaid(regExternalId);
    }

    public Optional<EventRegistrationItem> findByExternalId(UUID regExternalId) {
        return registrationRepo.findByExternalId(regExternalId);
    }

    public Optional<Integer> getEventRowIdByExternalId(UUID regExternalId) {
        return registrationRepo.getEventRowIdByExternalId(regExternalId);
    }

    public void storeTelegramChatId(UUID regExternalId, Long chatId) {
        try {
            registrationRepo.getGuestRowIdByExternalId(regExternalId)
                    .ifPresentOrElse(
                            guestRowId -> guestRepo.storeTelegramChatId(guestRowId, chatId),
                            () -> log.warnf("No guest found for reg=%s, chatId not stored", regExternalId)
                    );
        } catch (Exception e) {
            log.warnf("Failed to store chatId=%d for reg=%s: %s", chatId, regExternalId, e.getMessage());
        }
    }
}
