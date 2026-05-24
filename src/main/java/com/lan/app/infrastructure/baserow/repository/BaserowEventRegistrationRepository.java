package com.lan.app.infrastructure.baserow.repository;

import com.lan.app.domain.model.EventRegistration;
import com.lan.app.domain.model.EventRegistrationItem;
import com.lan.app.domain.model.Id;
import com.lan.app.infrastructure.baserow.client.BaserowEventClient;
import com.lan.app.infrastructure.baserow.client.BaserowGuestClient;
import com.lan.app.infrastructure.baserow.client.BaserowEventRegistrationClient;
import com.lan.app.infrastructure.baserow.dto.CreateEventRegistrationRowRequest;
import com.lan.app.infrastructure.baserow.dto.UpdateRegistrationIsPaidRequest;
import com.lan.app.infrastructure.baserow.mapper.BaserowEventRegistrationMapper;
import com.lan.app.repository.EventRegistrationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class BaserowEventRegistrationRepository extends AbstractBaserowRepository
        implements EventRegistrationRepository {

    private static final Logger log = Logger.getLogger(BaserowEventRegistrationRepository.class);

    private final int registrationsTableId;
    private final int eventTableId;
    private final int guestsTableId;

    private final BaserowEventRegistrationClient client;
    private final BaserowEventClient eventClient;
    private final BaserowGuestClient guestClient;
    private final BaserowEventRegistrationMapper mapper;

    @Inject
    public BaserowEventRegistrationRepository(
        @ConfigProperty(name = "baserow.events.registrations-table-id") int registrationsTableId,
        @ConfigProperty(name = "baserow.events.events-table-id") int eventTableId,
        @ConfigProperty(name = "baserow.guests.guests-table-id") int guestsTableId,
        @RestClient BaserowEventRegistrationClient client,
        @RestClient BaserowEventClient eventClient,
        @RestClient BaserowGuestClient guestClient,
        BaserowEventRegistrationMapper mapper
    ) {
        this.registrationsTableId = registrationsTableId;
        this.eventTableId = eventTableId;
        this.guestsTableId = guestsTableId;
        this.client = client;
        this.eventClient = eventClient;
        this.guestClient = guestClient;
        this.mapper = mapper;
    }

    @Override
    public EventRegistration create(Id eventId, Id guestId, int guestCount, String comment, String source) {
        var body = new CreateEventRegistrationRowRequest(
            List.of(eventId.internalId()),
            List.of(guestId.internalId()),
            guestCount,
            comment,
            source
        );
        var created = execute(() -> client.create(registrationsTableId, body));
        var fullRow = execute(() -> client.getByRowId(registrationsTableId, created.id()));
        return mapper.toDomain(fullRow);
    }

    @Override
    public Optional<Integer> getGuestRowIdByExternalId(UUID regExternalId) {
        var response = execute(() -> client.findByExternalIdRaw(registrationsTableId, regExternalId));
        if (response.results().isEmpty()) {
            log.warnf("Registration not found in Baserow for externalId=%s", regExternalId);
            return Optional.empty();
        }
        var reg = response.results().getFirst();
        if (reg.guestId() == null || reg.guestId().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(reg.guestId().getFirst().id());
    }

    @Override
    public Optional<Long> markPaid(UUID externalId) {
        var response = execute(() -> client.findByExternalIdRaw(registrationsTableId, externalId));
        if (response.results().isEmpty()) {
            log.warnf("Registration not found for externalId=%s", externalId);
            return Optional.empty();
        }
        var reg = response.results().getFirst();
        execute(() -> client.updateIsPaid(registrationsTableId, reg.id(), new UpdateRegistrationIsPaidRequest(true)));
        if (reg.guestId() == null || reg.guestId().isEmpty()) return Optional.empty();
        int guestRowId = reg.guestId().getFirst().id();
        try {
            var guest = execute(() -> guestClient.getByRowId(guestsTableId, guestRowId));
            return Optional.ofNullable(guest.telegramChatId());
        } catch (Exception e) {
            log.warnf("Could not fetch guest chatId for reg=%s: %s", externalId, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<EventRegistrationItem> findByGuestRowId(int guestRowId) {
        var registrations = execute(() ->
            client.findByGuestRowIdRaw(registrationsTableId, guestRowId).results()
        );

        var result = new ArrayList<EventRegistrationItem>();
        for (var reg : registrations) {
            if (reg.eventId() == null || reg.eventId().isEmpty()) continue;
            int eventRowId = reg.eventId().getFirst().id();
            try {
                var eventRow = execute(() -> eventClient.getByRowId(eventTableId, eventRowId));
                result.add(new EventRegistrationItem(eventRow.name(), eventRow.dateStart()));
            } catch (Exception e) {
                log.warnf("Could not fetch event rowId=%d for guestRowId=%d: %s",
                        eventRowId, guestRowId, e.getMessage());
            }
        }
        return result;
    }
}
