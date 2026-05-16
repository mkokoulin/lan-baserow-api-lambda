package com.lan.app.api.resource;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import com.lan.app.api.dto.response.EventResponse;
import com.lan.app.api.mapper.ApiEventMapper;
import com.lan.app.service.EventService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;


@Path("/events")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "web-users"})
@SecurityRequirement(name = "bearerAuth")
public class EventResource {

    private final EventService service;
    private final ApiEventMapper mapper;

    public EventResource(
        EventService service,
        ApiEventMapper mapper
    ) {
        this.service = service;
        this.mapper = mapper;
    }

    @GET
    public List<EventResponse> list() {
        var events = service.list();
        return events.stream()
            .map(mapper::toResponse)
            .toList();
    }

    @GET
    @Path("/{externalId}")
    public EventResponse get(
        @PathParam("externalId") UUID externalId
    ) {
        var event = service.get(externalId);
        return mapper.toResponse(event);
    }
}
