package com.lan.app.infrastructure.baserow.repository;

import com.lan.app.domain.model.CoworkingMeetingRoomBooking;
import com.lan.app.infrastructure.baserow.client.BaserowCoworkingGuestClient;
import com.lan.app.infrastructure.baserow.client.BaserowCoworkingMeetingRoomBookingClient;
import com.lan.app.infrastructure.baserow.dto.BaserowCoworkingMeetingRoomBookingRow;
import com.lan.app.infrastructure.baserow.mapper.BaserowCoworkingMeetingRoomBookingMapper;
import com.lan.app.repository.CoworkingMeetingRoomBookingRepository;
import com.lan.app.service.command.CreateCoworkingMeetingRoomBookingCommand;
import com.lan.app.service.command.UpdateCoworkingMeetingRoomBookingCommand;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.UUID;

@ApplicationScoped
public class BaserowCoworkingMeetingRoomBookingRepository extends AbstractBaserowRepository implements CoworkingMeetingRoomBookingRepository {

    private static final String ENTITY_NAME = "Coworking meeting room booking";

    private final int coworkingMeetingRoomBookingTableId;
    private final int coworkingGuestsTableId;

    private final BaserowCoworkingMeetingRoomBookingClient client;
    private final BaserowCoworkingGuestClient guestClient;
    private final BaserowCoworkingMeetingRoomBookingMapper mapper;

    BaserowCoworkingMeetingRoomBookingRepository(
        @ConfigProperty(name = "baserow.coworking.meeting-room-bookings-table-id") int coworkingMeetingRoomBookingTableId,
        @ConfigProperty(name = "baserow.coworking.guests-table-id") int coworkingGuestsTableId,
        @RestClient BaserowCoworkingMeetingRoomBookingClient meetingRoomBookingClient,
        @RestClient BaserowCoworkingGuestClient guestClient,
        BaserowCoworkingMeetingRoomBookingMapper mapper
    ) {
        this.coworkingMeetingRoomBookingTableId = coworkingMeetingRoomBookingTableId;
        this.coworkingGuestsTableId = coworkingGuestsTableId;
        this.client = meetingRoomBookingClient;
        this.guestClient = guestClient;
        this.mapper = mapper;
    }

    public CoworkingMeetingRoomBooking get(UUID externalId) {
        return executeWithEntityNotFound(
            () -> {
                var row = client.findUniqueByExternalId(coworkingMeetingRoomBookingTableId, externalId);
                return mapper.toDomain(row, resolveGuestExternalId(row));
            },
            ENTITY_NAME,
            externalId
        );
    }

    public CoworkingMeetingRoomBooking create(CreateCoworkingMeetingRoomBookingCommand cmd) {
        return execute(() -> {
            var req = mapper.toBaserowRequest(cmd);
            var row = client.create(coworkingMeetingRoomBookingTableId, req);
            return mapper.toDomain(row, resolveGuestExternalId(row));
        });
    }

    public CoworkingMeetingRoomBooking update(UUID externalId, UpdateCoworkingMeetingRoomBookingCommand cmd) {
        return executeWithEntityNotFound(
            () -> {
                var existing = client.findUniqueByExternalId(coworkingMeetingRoomBookingTableId, externalId);
                var patch = mapper.toBaserowPatch(cmd);
                var row = client.update(coworkingMeetingRoomBookingTableId, existing.id(), patch);
                return mapper.toDomain(row, resolveGuestExternalId(row));
            },
            ENTITY_NAME,
            externalId
        );
    }

    private UUID resolveGuestExternalId(BaserowCoworkingMeetingRoomBookingRow row) {
        return execute(() -> {
            var guestRowId = row.guestId().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Meeting room booking guest link is missing"))
                .id();

            try {
                return guestClient.getByRowId(coworkingGuestsTableId, guestRowId).externalId();
            } catch (WebApplicationException e) {
                if (isNotFound(e)) {
                    throw new IllegalStateException(
                        "Linked coworking guest was not found for booking externalId=" + row.externalId()
                    );
                }
                throw e;
            }
        });
    }
}
