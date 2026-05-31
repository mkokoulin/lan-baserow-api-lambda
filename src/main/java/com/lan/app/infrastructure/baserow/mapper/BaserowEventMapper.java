package com.lan.app.infrastructure.baserow.mapper;

import java.net.URI;
import java.util.List;

import com.lan.app.domain.model.Event;
import com.lan.app.domain.model.Id;
import com.lan.app.infrastructure.baserow.dto.BaserowEventRow;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaserowEventMapper {
    
    public Event toDomain(BaserowEventRow event) {
        var notifications = event.notifications() != null
            ? event.notifications().stream().map(n -> String.valueOf(n.id())).toList()
            : List.<String>of();

        String imageUrl = event.image() != null && !event.image().isEmpty()
            ? event.image().getFirst().url()
            : null;

        return new Event(
            new Id(event.id(), event.externalId()),
            event.name(),
            event.dateStart(),
            event.dateEnd(),
            event.description(),
            parseUri(event.externalRegistrationUrl()),
            event.registrationUrl(),
            parseUri(event.instagramUrl()),
            event.showForm(),
            notifications,
            event.comment(),
            event.isPin(),
            event.requiresPrepayment() != null && event.requiresPrepayment(),
            event.price(),
            imageUrl
        );
    }

    private URI parseUri(String raw) {
        if (raw == null || raw.isBlank()) return null;
        return URI.create(raw);
    }
}
