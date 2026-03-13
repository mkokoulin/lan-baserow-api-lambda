package com.lan.app.api.mapper;

import com.lan.app.api.dto.request.CreateCoworkingMeetingRoomBookingRequest;
import com.lan.app.api.dto.request.UpdateCoworkingGuestRequest;
import com.lan.app.api.dto.request.UpdateCoworkingMeetingRoomBookingRequest;
import com.lan.app.api.dto.response.CoworkingMeetingRoomBookingResponse;
import com.lan.app.domain.model.CoworkingMeetingRoomBooking;
import com.lan.app.service.command.CreateCoworkingMeetingRoomBookingCommand;
import com.lan.app.service.command.UpdateCoworkingMeetingRoomBookingCommand;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApiCoworkingMeetingRoomBookingMapper {

    public CreateCoworkingMeetingRoomBookingCommand toCommand(CreateCoworkingMeetingRoomBookingRequest req) {
        return new CreateCoworkingMeetingRoomBookingCommand(
            req.guestId(),
            req.dateStart(),
            req.dateEnd(),
            req.persons(),
            req.comment()
        );
    }

    public UpdateCoworkingMeetingRoomBookingCommand toCommand(UpdateCoworkingMeetingRoomBookingRequest req) {
        return new UpdateCoworkingMeetingRoomBookingCommand(
            req.dateStart(),
            req.dateEnd(),
            req.persons(),
            req.comment()
        );
    }

    public CoworkingMeetingRoomBookingResponse toResponse(CoworkingMeetingRoomBooking r) {
        return new CoworkingMeetingRoomBookingResponse(
            r.externalId(),
            r.guestId(),
            r.dateStart(),
            r.dateEnd(),
            r.persons(),
            r.comment()
        );
    }
}
