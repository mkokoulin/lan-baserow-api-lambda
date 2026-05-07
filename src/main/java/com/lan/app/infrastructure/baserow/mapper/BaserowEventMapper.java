package com.lan.app.infrastructure.baserow.mapper;

import java.net.URI;

import com.lan.app.domain.model.Event;
import com.lan.app.domain.model.EventClient;
import com.lan.app.domain.model.EventImage;
import com.lan.app.domain.model.EventType;
import com.lan.app.infrastructure.baserow.dto.BaserowEventRow;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaserowEventMapper {
    
    public Event toDomain(BaserowEventRow event) {
        var notificationTime =
            event.notificationTime()
                .stream()
                .map(nt -> nt.value())
                .toList();

        var showEvent =
            event.showEvent()
                .stream()
                .map(e -> EventClient.fromBaserow(e.value()))
                .toList();


        if (event.image().isEmpty()) {
            throw new IllegalStateException("Event id=%d has no image".formatted(event.id()));
        }
       
        EventImage image = new EventImage(event.image().getFirst().url());

        return new Event(
            event.externalId(),
            event.parentId(),
            event.name(),
            event.dateStart(),
            event.dateEnd(),
            event.description(),
            image,
            parseUri(event.externalRegistrationUrl()),
            event.registrationUrl(),
            parseUri(event.instagramUrl()),
            EventType.fromBaserow(event.type().value()),
            event.showForm(),
            notificationTime,
            event.comment(),
            showEvent,
            event.isPin()
        );
    }

    private URI parseUri(String raw) {
        if (raw == null || raw.isBlank()) return null;
        return URI.create(raw);
    }
}
