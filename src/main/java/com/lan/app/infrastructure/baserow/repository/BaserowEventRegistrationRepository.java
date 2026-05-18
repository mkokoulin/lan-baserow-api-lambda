package com.lan.app.infrastructure.baserow.repository;

import com.lan.app.domain.model.EventRegistration;
import com.lan.app.domain.model.EventRegistrationItem;
import com.lan.app.domain.model.Id;
import com.lan.app.infrastructure.baserow.client.BaserowEventClient;
import com.lan.app.infrastructure.baserow.client.BaserowEventRegistrationClient;
import com.lan.app.infrastructure.baserow.dto.CreateEventRegistrationRowRequest;
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

    private final BaserowEventRegistrationClient client;
    private final BaserowEventClient eventClient;
    private final BaserowEventRegistrationMapper mapper;

    @Inject
    public BaserowEventRegistrationRepository(
        @ConfigProperty(name = "baserow.events.registrations-table-id") int registrationsTableId,
        @ConfigProperty(name = "baserow.events.events-table-id") int eventTableId,
        @RestClient BaserowEventRegistrationClient client,
        @RestClient BaserowEventClient eventClient,
        BaserowEventRegistrationMapper mapper
    ) {
        this.registrationsTableId = registrationsTableId;
        this.eventTableId = eventTableId;
        this.client = client;
        this.eventClient = eventClient;
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
        return mapper.toDomain(created);
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
