package com.lan.app.service;

import com.lan.app.domain.exception.BusinessConflictException;
import com.lan.app.domain.exception.RegistrationNotFoundException;
import com.lan.app.domain.exception.ValidationException;
import com.lan.app.domain.model.EventRegistration;
import com.lan.app.domain.model.EventRegistrationItem;
import com.lan.app.domain.model.RegistrationActionResult;
import com.lan.app.repository.EventGuestRepository;
import com.lan.app.repository.EventRegistrationRepository;
import com.lan.app.repository.EventRepository;
import com.lan.app.service.command.CreateEventRegistrationCommand;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.time.Instant;
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
    private final EventCapacityService capacityService;

    public EventRegistrationService(
        EventRepository eventRepo,
        EventGuestRepository guestRepo,
        EventRegistrationRepository registrationRepo,
        EventCapacityService capacityService
    ) {
        this.eventRepo = eventRepo;
        this.guestRepo = guestRepo;
        this.registrationRepo = registrationRepo;
        this.capacityService = capacityService;
    }

    public EventRegistration create(CreateEventRegistrationCommand cmd) {
        var event = eventRepo.get(cmd.eventId());
        if (event.soldOut()) {
            throw new BusinessConflictException(
                "Event is sold out.",
                Map.of("eventId", event.id().externalId().toString(), "availableSpots", 0)
            );
        }
        Integer remaining = capacityService.remainingCapacity(event.maxCapacity(), event.id().internalId());
        if (remaining != null && cmd.guestCount() > remaining) {
            throw new BusinessConflictException(
                "Not enough seats left for the requested guest count.",
                Map.of(
                    "eventId", event.id().externalId().toString(),
                    "availableSpots", remaining
                )
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

    public void storeTelegramChatIdForGuest(UUID regExternalId, int guestRowId, Long chatId) {
        linkChatId(regExternalId, guestRowId, chatId);
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
                            guestRowId -> linkChatId(regExternalId, guestRowId, chatId),
                            () -> log.warnf("No guest found for reg=%s, chatId not stored", regExternalId)
                    );
        } catch (Exception e) {
            log.warnf("Failed to store chatId=%d for reg=%s: %s", chatId, regExternalId, e.getMessage());
        }
    }

    /**
     * Links chatId to guestRowId — unless a different guest row already owns that chatId (e.g. the
     * guest registered for a previous event and already has a canonical row), in which case this
     * registration is repointed to the existing guest instead of creating a second chatId-linked
     * duplicate that GET /events/v1/bot/my-registrations would never see.
     */
    private void linkChatId(UUID regExternalId, int guestRowId, Long chatId) {
        try {
            var existing = guestRepo.findByTelegramChatId(chatId);
            if (existing.isPresent() && existing.get().id().internalId() != guestRowId && regExternalId != null) {
                registrationRepo.relinkGuest(regExternalId, existing.get().id().internalId());
            } else {
                guestRepo.storeTelegramChatId(guestRowId, chatId);
            }
        } catch (Exception e) {
            log.warnf("Failed to link chatId=%d for guestRowId=%d (reg=%s): %s",
                chatId, guestRowId, regExternalId, e.getMessage());
        }
    }

    public RegistrationActionResult cancel(UUID regExternalId) {
        requireActiveRegistration(regExternalId);
        return registrationRepo.cancel(regExternalId)
            .orElseThrow(() -> new RegistrationNotFoundException(regExternalId.toString()));
    }

    public RegistrationActionResult updateGuestCount(UUID regExternalId, int newGuestCount) {
        if (newGuestCount < 1) {
            throw new ValidationException("guestCount must be at least 1.");
        }
        var item = requireActiveRegistration(regExternalId);

        int eventRowId = registrationRepo.getEventRowIdByExternalId(regExternalId)
            .orElseThrow(() -> new RegistrationNotFoundException(regExternalId.toString()));
        var event = eventRepo.getByRowId(eventRowId);
        if (event.maxCapacity() != null) {
            int currentTotal = capacityService.registeredGuestCount(eventRowId);
            int remainingExcludingSelf = event.maxCapacity() - (currentTotal - item.guestCount());
            if (newGuestCount > remainingExcludingSelf) {
                throw new BusinessConflictException(
                    "Not enough seats left for the requested guest count.",
                    Map.of(
                        "registrationId", regExternalId.toString(),
                        "availableSpots", Math.max(0, remainingExcludingSelf)
                    )
                );
            }
        }

        return registrationRepo.updateGuestCount(regExternalId, newGuestCount)
            .orElseThrow(() -> new RegistrationNotFoundException(regExternalId.toString()));
    }

    private EventRegistrationItem requireActiveRegistration(UUID regExternalId) {
        var item = registrationRepo.findByExternalId(regExternalId)
            .orElseThrow(() -> new RegistrationNotFoundException(regExternalId.toString()));
        if (item.isCancelled()) {
            throw new BusinessConflictException(
                "Registration is already cancelled.",
                Map.of("registrationId", regExternalId.toString())
            );
        }
        if (item.dateStart() == null || !item.dateStart().isAfter(Instant.now())) {
            throw new BusinessConflictException(
                "Event has already started; registration can no longer be modified.",
                Map.of("registrationId", regExternalId.toString())
            );
        }
        return item;
    }
}
