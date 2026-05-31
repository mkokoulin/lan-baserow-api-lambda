package com.lan.app.infrastructure.baserow.repository;

import com.baserow.repository.AbstractBaserowRepository;
import com.lan.app.domain.model.CoworkingSiteBooking;
import com.lan.app.infrastructure.baserow.client.BaserowCoworkingSiteBookingClient;
import com.lan.app.infrastructure.baserow.mapper.BaserowCoworkingSiteBookingMapper;
import com.lan.app.repository.CoworkingSiteBookingRepository;
import com.lan.app.service.command.CreateCoworkingSiteBookingCommand;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class BaserowCoworkingSiteBookingRepository extends AbstractBaserowRepository implements CoworkingSiteBookingRepository {

    private final int siteBookingsTableId;
    private final BaserowCoworkingSiteBookingClient client;
    private final BaserowCoworkingSiteBookingMapper mapper;

    BaserowCoworkingSiteBookingRepository(
        @ConfigProperty(name = "baserow.coworking.site-bookings-table-id") int siteBookingsTableId,
        @RestClient BaserowCoworkingSiteBookingClient client,
        BaserowCoworkingSiteBookingMapper mapper
    ) {
        this.siteBookingsTableId = siteBookingsTableId;
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    public CoworkingSiteBooking create(CreateCoworkingSiteBookingCommand cmd) {
        return execute(() -> {
            var req = mapper.toBaserowRequest(cmd);
            var row = client.create(siteBookingsTableId, req);
            return mapper.toDomain(row, cmd.tariff());
        });
    }
}
