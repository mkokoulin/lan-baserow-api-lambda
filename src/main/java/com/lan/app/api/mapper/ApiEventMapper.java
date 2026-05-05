package com.lan.app.api.mapper;

import com.lan.app.api.dto.response.EventResponse;
import com.lan.app.domain.model.Event;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApiEventMapper {
    
    public EventResponse toResponse(Event event) {
        return new EventResponse(
            event.id(),
            event.name(),
            event.dateStart(),
            event.dateEnd(),
            event.description(),
            event.externalRegistrationUrl(),
            event.registrationUrl(),
            event.instagramUrl(),
            event.type().toString(),
            event.showForm(),
            event.notificationTime(),
            event.comment(),
            event.showEvent()
        );
    }
}
