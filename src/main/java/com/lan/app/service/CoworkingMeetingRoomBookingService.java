package com.lan.app.service;

import com.lan.app.domain.model.CoworkingMeetingRoomBooking;
import com.lan.app.repository.CoworkingMeetingRoomBookingRepository;
import com.lan.app.service.command.CreateCoworkingMeetingRoomBookingCommand;
import com.lan.app.service.command.UpdateCoworkingMeetingRoomBookingCommand;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class CoworkingMeetingRoomBookingService {

    CoworkingMeetingRoomBookingRepository repo;

    public CoworkingMeetingRoomBookingService(CoworkingMeetingRoomBookingRepository repo) {
        this.repo = repo;
    }

    public CoworkingMeetingRoomBooking get(UUID id) {
        return repo.get(id);
    }

    public CoworkingMeetingRoomBooking create(CreateCoworkingMeetingRoomBookingCommand request) {
        return repo.create(request);
    }

    public CoworkingMeetingRoomBooking update(UUID externalId, UpdateCoworkingMeetingRoomBookingCommand patch) {
        return repo.update(externalId, patch);
    }
}
