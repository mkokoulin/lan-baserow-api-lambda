package com.lan.app.repository;

import com.lan.app.domain.model.CoworkingSiteBooking;
import com.lan.app.service.command.CreateCoworkingSiteBookingCommand;

public interface CoworkingSiteBookingRepository {
    CoworkingSiteBooking create(CreateCoworkingSiteBookingCommand cmd);
}
