package com.lan.app.infrastructure.baserow.mapper;

import com.lan.app.domain.model.CoworkingSiteBooking;
import com.lan.app.infrastructure.baserow.dto.BaserowCoworkingSiteBookingRow;
import com.lan.app.infrastructure.baserow.dto.CreateCoworkingSiteBookingRowRequest;
import com.lan.app.service.command.CreateCoworkingSiteBookingCommand;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class BaserowCoworkingSiteBookingMapper {

    // Maps tariff code to the row ID in the coworking tariffs table (824730)
    private static final Map<String, Integer> TARIFF_ROW_IDS = Map.of(
        "1h",    1,
        "4h",    2,
        "day",   34,
        "week",  35,
        "month", 36
    );

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
        Integer tariffRowId = TARIFF_ROW_IDS.get(cmd.tariff());
        List<Integer> tariffRowIds = tariffRowId != null ? List.of(tariffRowId) : List.of();
        return new CreateCoworkingSiteBookingRowRequest(
            cmd.firstName(),
            cmd.phone(),
            cmd.telegram(),
            tariffRowIds,
            cmd.bookingDate(),
            cmd.startTime(),
            cmd.endTime(),
            "Новая"
        );
    }
}
