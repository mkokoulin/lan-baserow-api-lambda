package com.lan.app.api.mapper;

import com.lan.app.api.dto.response.EventResponse;
import com.lan.app.domain.model.Event;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EventResponseMapper {

    public EventResponse toResponse(Event event) {
        return new EventResponse(
            event.id(),
            event.parentId(),
            event.name(),
            event.dateStart(),
            event.dateEnd(),
            event.description(),
            event.externalRegistrationUrl(),
            event.registrationUrl(),
            event.instagramUrl(),
            event.type().toString().toLowerCase(),
            event.showForm(),
            event.notificationTime(),
            event.comment(),
            event.showEvent()
        );
    }
}
