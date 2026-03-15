package com.lan.app.api.resource;

import com.lan.app.api.dto.response.CoworkingTariffResponse;
import com.lan.app.api.mapper.ApiCoworkingTariffMapper;
import com.lan.app.service.CoworkingTariffService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.UUID;

@Path("/coworking/tariffs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "web-users"})
public class CoworkingTariffResource {

    private final CoworkingTariffService service;
    private final ApiCoworkingTariffMapper mapper;

    public CoworkingTariffResource(
        CoworkingTariffService service,
        ApiCoworkingTariffMapper mapper
    ) {
        this.service = service;
        this.mapper = mapper;
    }

    @GET
    public List<CoworkingTariffResponse> list() {
        var tariffs = service.list();
        return tariffs.stream()
            .map(mapper::toResponse)
            .toList();
    }

    @GET
    @Path("/{externalId}")
    public CoworkingTariffResponse get(
        @PathParam("externalId") UUID externalId
    ) {
        var tariff = service.get(externalId);
        return mapper.toResponse(tariff);
    }
}
