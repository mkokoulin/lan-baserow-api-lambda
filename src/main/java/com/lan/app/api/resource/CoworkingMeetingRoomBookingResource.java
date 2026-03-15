package com.lan.app.api.resource;

import com.lan.app.api.dto.request.CreateCoworkingMeetingRoomBookingRequest;
import com.lan.app.api.dto.request.UpdateCoworkingMeetingRoomBookingRequest;
import com.lan.app.api.dto.response.CoworkingMeetingRoomBookingResponse;
import com.lan.app.api.mapper.ApiCoworkingMeetingRoomBookingMapper;
import com.lan.app.service.CoworkingMeetingRoomBookingService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.UUID;

@Path("/coworking/meeting-room-booking")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "web-users"})
public class CoworkingMeetingRoomBookingResource {

    private final CoworkingMeetingRoomBookingService service;
    private final ApiCoworkingMeetingRoomBookingMapper mapper;

    public CoworkingMeetingRoomBookingResource(
        CoworkingMeetingRoomBookingService service,
        ApiCoworkingMeetingRoomBookingMapper mapper
    ) {
        this.service = service;
        this.mapper = mapper;
    }

    @GET
    @Path("/{externalId}")
    public CoworkingMeetingRoomBookingResponse get(
        @PathParam("externalId") UUID externalId
    ) {
        var meetingRoomBooking = service.get(externalId);
        return mapper.toResponse(meetingRoomBooking);
    }

    @POST
    public Response create(@Valid CreateCoworkingMeetingRoomBookingRequest req) {
        var command = mapper.toCommand(req);
        var created = service.create(command);
        return Response.created(URI.create("/coworking/guests/" + created.externalId()))
            .entity(mapper.toResponse(created))
            .build();
    }

    @PATCH
    @Path("/{externalId}")
    public Response update(
        @PathParam("externalId") UUID externalId,
        @Valid UpdateCoworkingMeetingRoomBookingRequest req
    ) {
        var command = mapper.toCommand(req);
        var created = service.update(externalId, command);
        return Response.created(URI.create("/coworking/meeting-room-booking" + created.externalId()))
            .entity(mapper.toResponse(created))
            .build();
    }
}
