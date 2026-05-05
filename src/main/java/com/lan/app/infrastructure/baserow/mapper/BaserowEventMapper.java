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
    
    public Event toDomain(BaserowEventRow row) {
        var notificationTime =
            row.notificationTime()
                .stream()
                .map(nt -> nt.value())
                .toList();

        var showEvent =
            row.showEvent()
                .stream()
                .map(se -> EventClient.fromBaserow(se.value()))
                .toList();


        if (row.image().isEmpty()) {
            throw new IllegalStateException("Event id=%d has no image".formatted(row.id()));
        }
       
        EventImage image = new EventImage(row.image().getFirst().url());

        return new Event(
            row.externalId(),
            row.name(),
            row.dateStart(),
            row.dateEnd(),
            row.description(),
            image,
            parseUri(row.externalRegistrationUrl()),
            row.registrationUrl(),
            parseUri(row.instagramUrl()),
            EventType.fromBaserow(row.type().value()),
            row.showForm(),
            notificationTime,
            row.comment(),
            showEvent
        );
    }

    private URI parseUri(String raw) {
        if (raw == null || raw.isBlank()) return null;
        return URI.create(raw);
    }
}
