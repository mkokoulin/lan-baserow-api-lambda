package com.lan.app.infrastructure.baserow.mapper;

import com.lan.app.domain.model.EventGuest;
import com.lan.app.domain.model.Id;
import com.lan.app.infrastructure.baserow.dto.BaserowGuestRow;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaserowEventGuestMapper {

    public EventGuest toDomain(BaserowGuestRow row) {
        return new EventGuest(
            new Id(row.id(), row.externalId()),
            row.firstName(),
            row.lastName(),
            row.telegram(),
            row.phone(),
            row.source() != null ? row.source().value() : null,
            row.telegramChatId() != null ? row.telegramChatId() : -1
        );
    }
}
