package com.lan.app.repository;

import com.lan.app.domain.model.CoworkingMeetingRoomBooking;
import com.lan.app.service.command.CreateCoworkingMeetingRoomBookingCommand;
import com.lan.app.service.command.UpdateCoworkingMeetingRoomBookingCommand;

import java.util.UUID;

public interface CoworkingMeetingRoomBookingRepository {
    CoworkingMeetingRoomBooking get(UUID id);
    CoworkingMeetingRoomBooking create(CreateCoworkingMeetingRoomBookingCommand request);
    CoworkingMeetingRoomBooking update(UUID externalId, UpdateCoworkingMeetingRoomBookingCommand patch);
}
