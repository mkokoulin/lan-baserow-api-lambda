package com.lan.app.api.mapper;

import com.lan.app.api.dto.request.EventRegistrationCreateRequest;
import com.lan.app.api.dto.response.EventRegistrationResponse;
import com.lan.app.domain.model.EventRegistration;
import com.lan.app.service.command.CreateEventRegistrationCommand;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EventRegistrationMapper {

    public CreateEventRegistrationCommand toCommand(EventRegistrationCreateRequest req) {
        return new CreateEventRegistrationCommand(
            req.eventId(),
            req.guestId(),
            req.guestComment(),
            req.guestCount(),
            req.source()
        );
    }

    public EventRegistrationResponse toResponse(EventRegistration registration) {
        var eventIdStr = registration.eventId().externalId() != null
            ? registration.eventId().externalId().toString()
            : String.valueOf(registration.eventId().internalId());
        var guestIdStr = registration.guestId().externalId() != null
            ? registration.guestId().externalId().toString()
            : String.valueOf(registration.guestId().internalId());
        return new EventRegistrationResponse(
            registration.id().externalId().toString(),
            eventIdStr,
            guestIdStr,
            registration.comment(),
            registration.guestCount(),
            registration.isPaid()
        );
    }
}
