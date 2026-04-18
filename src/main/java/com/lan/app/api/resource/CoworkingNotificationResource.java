package com.lan.app.api.resource;

import com.lan.app.api.dto.response.CoworkingNotificationResponse;
import com.lan.app.api.mapper.ApiCoworkingNotificationMapper;
import com.lan.app.service.CoworkingNotificationService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/coworking/notifications")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "web-users"})
@Tag(
    name = "Coworking Notifications",
    description = "Manage coworking notifications: retrieve the list of notifications available to the current user"
)
@SecurityRequirement(name = "bearerAuth")
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
    @Operation(
        operationId = "listCoworkingNotifications",
        summary = "List all coworking notifications",
        description = "Returns the full list of coworking notifications. " +
            "Accessible to users with the 'admin' or 'web-users' role."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Notifications retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(
                    implementation = CoworkingNotificationResponse.class,
                    type = SchemaType.ARRAY
                )
            )
        ),
        @APIResponse(
            responseCode = "401",
            description = "User is not authenticated"
        ),
        @APIResponse(
            responseCode = "403",
            description = "User does not have permission to access this resource"
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public List<CoworkingNotificationResponse> list() {
        return service.list().stream().map(mapper::toResponse).toList();
    }
}