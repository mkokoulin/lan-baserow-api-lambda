package com.lan.app.infrastructure.baserow.mapper;

import com.lan.app.domain.model.EventRegistration;
import com.lan.app.domain.model.Id;
import com.lan.app.infrastructure.baserow.dto.BaserowRegistrationRow;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class BaserowEventRegistrationMapper {

    public EventRegistration toDomain(BaserowRegistrationRow row) {
        var eventLink = row.eventId().getFirst();
        var guestLink = row.guestId().getFirst();

        UUID guestExternalId = parseUuid(guestLink.value());

        return new EventRegistration(
            new Id(row.id(), row.externalId()),
            new Id(eventLink.id(), null),
            new Id(guestLink.id(), guestExternalId),
            row.guestCount(),
            row.guestComment(),
            row.source() != null ? row.source().value() : null,
            row.isPaid()
        );
    }

    private UUID parseUuid(String value) {
        try {
            return value != null ? UUID.fromString(value) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
