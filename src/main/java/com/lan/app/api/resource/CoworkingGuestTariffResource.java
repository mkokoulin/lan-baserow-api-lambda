package com.lan.app.api.resource;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import com.lan.app.api.dto.request.CreateCoworkingGuestTariffRequest;
import com.lan.app.api.dto.response.CoworkingGuestTariffResponse;
import com.lan.app.api.mapper.ApiCoworkingGuestTariffMapper;
import com.lan.app.service.CoworkingGuestTariffService;

import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
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

    @POST
    public CoworkingGuestTariffResponse create(@NotNull @Valid CreateCoworkingGuestTariffRequest body) {
        var created = service.create(body.guestId(), body.tariffId());
        return mapper.toResponse(created);
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

    @POST
    @Path("/{externalId}/deduct-day")
    @Operation(
        operationId = "deductGuestTariffDay",
        summary = "Deduct one day from a guest tariff",
        description = "Increments daysUsed by 1 for the specified guest tariff. Returns the updated record."
    )
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Day deducted successfully"),
        @APIResponse(responseCode = "401", description = "User is not authenticated"),
        @APIResponse(responseCode = "403", description = "User does not have permission"),
        @APIResponse(responseCode = "404", description = "Guest tariff not found"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public CoworkingGuestTariffResponse deductDay(
        @PathParam("externalId") UUID externalId
    ) {
        var updated = service.deductDay(externalId);
        return mapper.toResponse(updated);
    }
}