package com.lan.app.infrastructure.baserow.mapper;

import com.lan.app.domain.model.CoworkingMeetingRoomBooking;
import com.lan.app.infrastructure.baserow.dto.BaserowCoworkingMeetingRoomBookingRow;
import com.lan.app.infrastructure.baserow.dto.CreateCoworkingMeetingRoomBookingRowRequest;
import com.lan.app.infrastructure.baserow.dto.UpdateCoworkingGuestRowRequest;
import com.lan.app.infrastructure.baserow.dto.UpdateCoworkingMeetingRoomBookingRowRequest;
import com.lan.app.service.command.CreateCoworkingMeetingRoomBookingCommand;
import com.lan.app.service.command.UpdateCoworkingGuestCommand;
import com.lan.app.service.command.UpdateCoworkingMeetingRoomBookingCommand;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class BaserowCoworkingMeetingRoomBookingMapper {

    public CoworkingMeetingRoomBooking toDomain(BaserowCoworkingMeetingRoomBookingRow row, UUID guestId) {
        return new CoworkingMeetingRoomBooking(
            row.externalId(),
            guestId,
            row.dateStart(),
            row.dateEnd(),
            row.persons(),
            row.comment()
        );
    }

    public CreateCoworkingMeetingRoomBookingRowRequest toBaserowRequest(CreateCoworkingMeetingRoomBookingCommand cmd) {
        if (cmd == null) {
            return null;
        }

        return new CreateCoworkingMeetingRoomBookingRowRequest(
            cmd.guestId(),
            cmd.dateStart(),
            cmd.dateEnd(),
            cmd.persons(),
            cmd.comment()
        );
    }

    public UpdateCoworkingMeetingRoomBookingRowRequest toBaserowPatch(UpdateCoworkingMeetingRoomBookingCommand cmd) {
        if (cmd == null) {
            return null;
        }

        return new UpdateCoworkingMeetingRoomBookingRowRequest(
            cmd.dateStart(),
            cmd.dateEnd(),
            cmd.persons(),
            cmd.comment()
        );
    }
}
