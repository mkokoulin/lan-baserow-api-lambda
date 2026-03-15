package com.lan.app.api.resource;

import com.lan.app.api.dto.response.CoworkingGuestResponse;
import com.lan.app.api.dto.request.CreateCoworkingGuestRequest;
import com.lan.app.api.dto.request.UpdateCoworkingGuestRequest;
import com.lan.app.api.mapper.ApiCoworkingGuestMapper;
import com.lan.app.service.CoworkingGuestService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.UUID;

@Path("/coworking/guests")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "web-users"})
public class CoworkingGuestResource {

    private final CoworkingGuestService service;
    private final ApiCoworkingGuestMapper mapper;

    public CoworkingGuestResource(CoworkingGuestService service, ApiCoworkingGuestMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GET
    @Path("/{externalId}")
    public CoworkingGuestResponse get(
        @PathParam("externalId") UUID externalId
    ) {
        var result = service.get(externalId);
        return mapper.toResponse(result);
    }

    @POST
    public Response create(@Valid CreateCoworkingGuestRequest req) {
        var created = service.create(req.firstName(), req.lastName(), req.phone(), req.telegram());
        return Response.created(URI.create("/coworking/guests/" + created.externalId()))
            .entity(mapper.toResponse(created))
            .build();
    }

    @PATCH
    @Path("/{externalId}")
    public CoworkingGuestResponse update(
        @PathParam("externalId") UUID externalId,
        @Valid UpdateCoworkingGuestRequest req
    ) {
        var command = mapper.toCommand(req);
        var updated = service.update(externalId, command);
        return mapper.toResponse(updated);
    }
}
