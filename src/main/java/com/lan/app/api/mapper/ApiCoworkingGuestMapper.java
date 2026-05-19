package com.lan.app.api.mapper;

import com.lan.app.api.dto.response.CoworkingGuestResponse;
import com.lan.app.api.dto.request.UpdateCoworkingGuestRequest;
import com.lan.app.domain.model.CoworkingGuest;
import com.lan.app.service.command.UpdateCoworkingGuestCommand;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApiCoworkingGuestMapper {

    public CoworkingGuestResponse toResponse(CoworkingGuest r) {
        return new CoworkingGuestResponse(
            r.externalId(),
            r.telegramChatId(),
            r.firstName(),
            r.lastName(),
            r.telegram(),
            r.phone()
        );
    }

    public UpdateCoworkingGuestCommand toCommand(UpdateCoworkingGuestRequest req) {
        return new UpdateCoworkingGuestCommand(
            req.firstName(),
            req.lastName(),
            req.phone(),
            req.telegram()
        );
    }
}
