package com.lan.app.api.resource;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.lan.app.api.dto.response.CoworkingNotificationResponse;
import com.lan.app.api.mapper.ApiCoworkingNotificationMapper;
import com.lan.app.service.CoworkingNotificationService;

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
@Tag(
    name = "Coworking News",
    description = "Manage coworking news: fetch news details"
)
@SecurityRequirement(name = "bearerAuth")
public class CoworkingNewResource {

    // private final CoworkingNewService service;
    // private final ApiCoworkingNewMapper mapper;

    // public CoworkingNewResource(
    //     CoworkingNewService service,
    //     ApiCoworkingNewMapper mapper
    // ) {
    //     this.service = service;
    //     this.mapper = mapper;
    // }

    // @GET
    // @Operation(
    //     operationId = "listCoworkingNews",
    //     summary = "List all coworking news",
    //     description = "Returns the full list of coworking news. " +
    //         "Accessible to users with the 'admin' or 'web-users' role."
    // )
    // @APIResponses({
    //     @APIResponse(
    //         responseCode = "200",
    //         description = "News retrieved successfully",
    //         content = @Content(
    //             mediaType = MediaType.APPLICATION_JSON,
    //             schema = @Schema(
    //                 implementation = CoworkingNewsResponse.class,
    //                 type = SchemaType.ARRAY
    //             )
    //         )
    //     ),
    //     @APIResponse(
    //         responseCode = "401",
    //         description = "User is not authenticated"
    //     ),
    //     @APIResponse(
    //         responseCode = "403",
    //         description = "User does not have permission to access this resource"
    //     ),
    //     @APIResponse(
    //         responseCode = "500",
    //         description = "Internal server error"
    //     )
    // })
    // public List<CoworkingNewsResponse> list() {
    //     return service.list().stream().map(mapper::toResponse).toList();
    // }
}
