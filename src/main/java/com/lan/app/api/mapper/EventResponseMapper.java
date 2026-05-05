package com.lan.app.api.mapper;

import com.lan.app.api.dto.response.EventResponse;
import com.lan.app.domain.model.Event;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EventResponseMapper {

    public EventResponse toResponse(Event e) {
        return new EventResponse(
            e.id(),
            e.name(),
            e.dateStart(),
            e.dateEnd(),
            e.description(),
            e.externalRegistrationUrl(),
            e.registrationUrl(),
            e.instagramUrl(),
            e.type().toString().toLowerCase(),
            e.showForm(),
            e.notificationTime(),
            e.comment(),
            e.showEvent()
        );
    }
}
