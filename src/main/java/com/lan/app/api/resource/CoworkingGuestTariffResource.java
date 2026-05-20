package com.lan.app.api.resource;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import com.lan.app.api.dto.response.CoworkingGuestTariffResponse;
import com.lan.app.api.mapper.ApiCoworkingGuestTariffMapper;
import com.lan.app.service.CoworkingGuestTariffService;

import jakarta.ws.rs.core.MediaType;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;

@Path("/coworking/v1/guest-tariffs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "web-users"})

@SecurityRequirement(name = "bearerAuth")
public class CoworkingGuestTariffResource {

    private final CoworkingGuestTariffService service;
    private final ApiCoworkingGuestTariffMapper mapper;

    public CoworkingGuestTariffResource(
        CoworkingGuestTariffService service,
        ApiCoworkingGuestTariffMapper mapper
    ) {
        this.service = service;
        this.mapper = mapper;
    }

    @GET
    public List<CoworkingGuestTariffResponse> list(
        @QueryParam("guestId") UUID guestId
    ) {
        var guestTariffs = service.list();
        return guestTariffs.stream()
            .map(mapper::toResponse)
            .filter(gt -> gt.guestId().equals(guestId))
            .toList();
    }

    @GET
    @Path("/{externalId}")
    public CoworkingGuestTariffResponse get(
        @PathParam("externalId") UUID externalId
    ) {
        var guestTariff = service.get(externalId);
        return mapper.toResponse(guestTariff);
    }
}