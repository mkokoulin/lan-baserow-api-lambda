package com.lan.app.infrastructure.baserow.mapper;

import com.lan.app.domain.model.EventGuest;
import com.lan.app.domain.model.Id;
import com.lan.app.infrastructure.baserow.dto.BaserowEventGuestRow;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaserowEventGuestMapper {

    public EventGuest toDomain(BaserowEventGuestRow row) {
        return new EventGuest(
            new Id(row.id(), row.externalId()),
            row.firstName(),
            row.lastName(),
            row.telegram(),
            row.phone(),
            row.source() != null ? row.source().value() : null,
            row.chatId() != null ? row.chatId() : -1
        );
    }
}
