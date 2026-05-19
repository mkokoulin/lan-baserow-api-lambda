package com.lan.app.infrastructure.baserow.mapper;

import com.lan.app.domain.model.CoworkingGuest;
import com.lan.app.infrastructure.baserow.dto.BaserowCoworkingGuestRow;
import com.lan.app.infrastructure.baserow.dto.UpdateCoworkingGuestRowRequest;
import com.lan.app.service.command.UpdateCoworkingGuestCommand;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaserowCoworkingGuestMapper {

    public CoworkingGuest toDomain(BaserowCoworkingGuestRow coworkingGuest) {
        return new CoworkingGuest(
            coworkingGuest.externalId(),
            coworkingGuest.telegramChatId(),
            coworkingGuest.firstName(),
            coworkingGuest.lastName(),
            coworkingGuest.telegram(),
            coworkingGuest.phone()
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
