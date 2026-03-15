package com.lan.app.api.resource;

import com.lan.app.api.dto.response.CoworkingNotificationResponse;
import com.lan.app.api.mapper.ApiCoworkingNotificationMapper;
import com.lan.app.service.CoworkingNotificationService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/coworking/notifications")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "web-users"})
public class CoworkingNotificationResource {

    private final CoworkingNotificationService service;
    private final ApiCoworkingNotificationMapper mapper;

    public CoworkingNotificationResource(
        CoworkingNotificationService service,
        ApiCoworkingNotificationMapper mapper
    ) {
        this.service = service;
        this.mapper = mapper;
    }

    @GET
    public List<CoworkingNotificationResponse> list() {
        return service.list().stream().map(mapper::toResponse).toList();
    }
}
