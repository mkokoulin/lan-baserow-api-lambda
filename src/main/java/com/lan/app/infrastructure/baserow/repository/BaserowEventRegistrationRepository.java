package com.lan.app.infrastructure.baserow.repository;

import com.lan.app.domain.model.EventRegistration;
import com.lan.app.domain.model.Id;
import com.lan.app.infrastructure.baserow.client.BaserowEventRegistrationClient;
import com.lan.app.infrastructure.baserow.dto.CreateEventRegistrationRowRequest;
import com.lan.app.infrastructure.baserow.mapper.BaserowEventRegistrationMapper;
import com.lan.app.repository.EventRegistrationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@ApplicationScoped
public class BaserowEventRegistrationRepository implements EventRegistrationRepository {

    private final int registrationsTableId;

    private final BaserowEventRegistrationClient client;
    private final BaserowEventRegistrationMapper mapper;

    @Inject
    public BaserowEventRegistrationRepository(
        @ConfigProperty(name = "baserow.events.registrations-table-id") int registrationsTableId,
        @RestClient BaserowEventRegistrationClient client,
        BaserowEventRegistrationMapper mapper
    ) {
        this.registrationsTableId = registrationsTableId;
        this.client = client;
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
        var created = client.create(registrationsTableId, body);
        return mapper.toDomain(created);
    }
}
