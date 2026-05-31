package com.lan.app.service;

import com.lan.app.domain.model.CoworkingSiteBooking;
import com.lan.app.repository.CoworkingSiteBookingRepository;
import com.lan.app.service.command.CreateCoworkingSiteBookingCommand;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CoworkingSiteBookingService {

    private final CoworkingSiteBookingRepository repo;

    public CoworkingSiteBookingService(CoworkingSiteBookingRepository repo) {
        this.repo = repo;
    }

    public CoworkingSiteBooking create(CreateCoworkingSiteBookingCommand cmd) {
        return repo.create(cmd);
    }
}
