package com.lan.app.infrastructure.baserow.mapper;

import com.lan.app.domain.model.EventRegistration;
import com.lan.app.domain.model.Id;
import com.lan.app.infrastructure.baserow.dto.BaserowRegistrationRow;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.UUID;

@ApplicationScoped
public class BaserowEventRegistrationMapper {

    private static final Logger log = Logger.getLogger(BaserowEventRegistrationMapper.class);

    public EventRegistration toDomain(BaserowRegistrationRow row) {
        return toDomain(row, row.externalId());
    }

    public EventRegistration toDomain(BaserowRegistrationRow row, UUID externalId) {
        var eventLinks = row.eventId();
        var guestLinks = row.guestId();

        if (eventLinks == null || eventLinks.isEmpty()) {
            log.warnf("BaserowRegistrationRow %d has null/empty eventId links", row.id());
        }
        if (guestLinks == null || guestLinks.isEmpty()) {
            log.warnf("BaserowRegistrationRow %d has null/empty guestId links", row.id());
        }

        var eventLink = (eventLinks != null && !eventLinks.isEmpty()) ? eventLinks.getFirst() : null;
        var guestLink = (guestLinks != null && !guestLinks.isEmpty()) ? guestLinks.getFirst() : null;

        UUID guestExternalId = guestLink != null ? parseUuid(guestLink.value()) : null;

        return new EventRegistration(
            new Id(row.id(), externalId),
            new Id(eventLink != null ? eventLink.id() : null, null),
            new Id(guestLink != null ? guestLink.id() : null, guestExternalId),
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
