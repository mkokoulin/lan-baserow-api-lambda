package com.lan.app.api.resource;

import com.lan.app.api.dto.response.CoworkingActiveTariffListItemResponse;
import com.lan.app.api.dto.response.CoworkingActiveTariffResponse;
import com.lan.app.api.mapper.ApiCoworkingActiveTariffMapper;
import com.lan.app.service.CoworkingActiveTariffService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.UUID;

@Path("/coworking/active-tariffs")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "web-users"})
public class CoworkingActiveTariffResource {

    private final CoworkingActiveTariffService service;
    private final ApiCoworkingActiveTariffMapper mapper;

    public CoworkingActiveTariffResource(
        CoworkingActiveTariffService service,
        ApiCoworkingActiveTariffMapper mapper
    ) {
        this.service = service;
        this.mapper = mapper;
    }

    @GET
    public List<CoworkingActiveTariffListItemResponse> list() {
        var tariffs = service.list();
        return tariffs.stream()
            .map(mapper::toListItem)
            .toList();
    }

    @GET
    @Path("/{externalId}")
    public CoworkingActiveTariffResponse get(
        @PathParam("externalId") UUID externalId
    ) {
        var tariff = service.get(externalId);
        return mapper.toResponse(tariff);
    }
}
