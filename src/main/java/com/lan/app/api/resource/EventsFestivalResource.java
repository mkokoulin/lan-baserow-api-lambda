package com.lan.app.api.resource;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import com.lan.app.api.dto.response.FestivalResponse;
import com.lan.app.api.mapper.ApiEventsFestivalMapper;
import com.lan.app.service.EventsFestivalService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;


@Path("/festivals")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "web-users"})
@SecurityRequirement(name = "bearerAuth")
public class EventsFestivalResource {

    private final EventsFestivalService service;
    private final ApiEventsFestivalMapper mapper;

    public EventsFestivalResource(
        EventsFestivalService service,
        ApiEventsFestivalMapper mapper
    ) {
        this.service = service;
        this.mapper = mapper;
    }

    @GET
    public List<FestivalResponse> list() {
        var festivals = service.list();
        return festivals.stream()
            .map(mapper::toResponse)
            .toList();
    }

    @GET
    @Path("/{externalId}")
    public FestivalResponse get(
        @PathParam("externalId") UUID externalId
    ) {
        var festival = service.get(externalId);
        return mapper.toResponse(festival);
    }
}
