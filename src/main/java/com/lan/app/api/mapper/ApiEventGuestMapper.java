package com.lan.app.api.mapper;

import com.lan.app.api.dto.response.EventGuestResponse;
import com.lan.app.domain.model.EventGuest;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApiEventGuestMapper {

    public EventGuestResponse toResponse(EventGuest guest) {
        return new EventGuestResponse(
            guest.id().externalId().toString(),
            guest.firstName(),
            guest.lastName(),
            guest.telegram(),
            guest.phone(),
            guest.chatId()
        );
    }
}
