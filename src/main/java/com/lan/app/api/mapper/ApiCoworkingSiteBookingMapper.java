package com.lan.app.api.mapper;

import com.lan.app.api.dto.request.CreateCoworkingSiteBookingRequest;
import com.lan.app.api.dto.response.CoworkingSiteBookingResponse;
import com.lan.app.domain.model.CoworkingSiteBooking;
import com.lan.app.service.command.CreateCoworkingSiteBookingCommand;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApiCoworkingSiteBookingMapper {

    public CreateCoworkingSiteBookingCommand toCommand(CreateCoworkingSiteBookingRequest req) {
        return new CreateCoworkingSiteBookingCommand(
            req.externalId(),
            req.firstName(),
            req.phone(),
            req.telegram(),
            req.tariff(),
            req.bookingDate(),
            req.startTime(),
            req.endTime()
        );
    }

    public CoworkingSiteBookingResponse toResponse(CoworkingSiteBooking booking) {
        return new CoworkingSiteBookingResponse(booking.externalId(), "Новая");
    }
}
