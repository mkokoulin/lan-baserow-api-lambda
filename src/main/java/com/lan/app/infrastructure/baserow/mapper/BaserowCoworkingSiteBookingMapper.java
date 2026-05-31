package com.lan.app.infrastructure.baserow.mapper;

import com.lan.app.domain.model.CoworkingSiteBooking;
import com.lan.app.infrastructure.baserow.dto.BaserowCoworkingSiteBookingRow;
import com.lan.app.infrastructure.baserow.dto.CreateCoworkingSiteBookingRowRequest;
import com.lan.app.service.command.CreateCoworkingSiteBookingCommand;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaserowCoworkingSiteBookingMapper {

    public CoworkingSiteBooking toDomain(BaserowCoworkingSiteBookingRow row, String tariff) {
        return new CoworkingSiteBooking(
            row.externalId(),
            row.firstName(),
            row.phone(),
            row.telegram(),
            tariff,
            row.bookingDate(),
            row.startTime(),
            row.endTime()
        );
    }

    public CreateCoworkingSiteBookingRowRequest toBaserowRequest(CreateCoworkingSiteBookingCommand cmd) {
        return new CreateCoworkingSiteBookingRowRequest(
            cmd.externalId(),
            cmd.firstName(),
            cmd.phone(),
            cmd.telegram(),
            cmd.tariff(),
            cmd.bookingDate(),
            cmd.startTime(),
            cmd.endTime(),
            "Новая"
        );
    }
}
