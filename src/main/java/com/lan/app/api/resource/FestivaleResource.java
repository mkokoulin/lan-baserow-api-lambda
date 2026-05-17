package com.lan.app.api.resource;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import com.lan.app.api.dto.response.FestivaleResponse;
import com.lan.app.api.mapper.ApiFestivaleMapper;
import com.lan.app.service.FestivaleService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;


@Path("/festivales")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "web-users"})
@SecurityRequirement(name = "bearerAuth")
public class FestivaleResource {

    private final FestivaleService service;
    private final ApiFestivaleMapper mapper;

    public FestivaleResource(
        FestivaleService service,
        ApiFestivaleMapper mapper
    ) {
        this.service = service;
        this.mapper = mapper;
    }

    @GET
    public List<FestivaleResponse> list() {
        var events = service.list();
        return events.stream()
            .map(mapper::toResponse)
            .toList();
    }

    @GET
    @Path("/{externalId}")
    public FestivaleResponse get(
        @PathParam("externalId") UUID externalId
    ) {
        var festivale = service.get(externalId);
        return mapper.toResponse(festivale);
    }
}
