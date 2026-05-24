package com.lan.app.infrastructure.baserow.mapper;

import com.lan.app.domain.model.CoworkingGuest;
import com.lan.app.infrastructure.baserow.dto.BaserowGuestRow;
import com.lan.app.infrastructure.baserow.dto.UpdateCoworkingGuestRowRequest;
import com.lan.app.service.command.UpdateCoworkingGuestCommand;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaserowCoworkingGuestMapper {

    public CoworkingGuest toDomain(BaserowGuestRow row) {
        return new CoworkingGuest(
            row.externalId(),
            row.telegramChatId(),
            row.firstName(),
            row.lastName(),
            row.telegram(),
            row.phone()
        );
    }

    public UpdateCoworkingGuestRowRequest toBaserowPatch(UpdateCoworkingGuestCommand cmd) {
        if (cmd == null) {
            return null;
        }

        return new UpdateCoworkingGuestRowRequest(
            cmd.firstName(),
            cmd.lastName(),
            cmd.phone(),
            cmd.telegram()
        );
    }
}
