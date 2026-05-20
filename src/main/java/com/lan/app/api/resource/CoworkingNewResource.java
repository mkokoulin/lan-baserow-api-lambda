package com.lan.app.api.resource;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import com.lan.app.api.dto.response.CoworkingNewResponse;
import com.lan.app.api.mapper.ApiCoworkingNewMapper;
import com.lan.app.service.CoworkingNewService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/coworking/v1/news")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "web-users"})
@SecurityRequirement(name = "bearerAuth")
public class CoworkingNewResource {

    private final CoworkingNewService service;
    private final ApiCoworkingNewMapper mapper;

    public CoworkingNewResource(
        CoworkingNewService service,
        ApiCoworkingNewMapper mapper
    ) {
        this.service = service;
        this.mapper = mapper;
    }

    @GET
    public List<CoworkingNewResponse> list() {
        return service.list().stream().map(mapper::toResponse).toList();
    }
}
