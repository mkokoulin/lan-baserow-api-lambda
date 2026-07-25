package com.lan.app.api.resource;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.lan.app.api.dto.response.InitiativeResponse;
import com.lan.app.api.mapper.ApiInitiativeMapper;
import com.lan.app.service.InitiativeService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/coworking/v1/initiatives")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "web-users"})
@Tag(
    name = "Initiatives",
    description = "Community and social initiatives published on the initiatives page"
)
@SecurityRequirement(name = "bearerAuth")
public class InitiativeResource {

    private final InitiativeService service;
    private final ApiInitiativeMapper mapper;

    public InitiativeResource(
        InitiativeService service,
        ApiInitiativeMapper mapper
    ) {
        this.service = service;
        this.mapper = mapper;
    }

    @GET
    @Operation(
        operationId = "listInitiatives",
        summary = "List all initiatives",
        description = "Returns the full list of published community/social initiatives. " +
            "Accessible to users with the 'admin' or 'web-users' role."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Initiatives retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(
                    implementation = InitiativeResponse.class,
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
    public List<InitiativeResponse> list() {
        var initiatives = service.list();
        return initiatives.stream()
            .map(mapper::toResponse)
            .toList();
    }

    @GET
    @Path("/{externalId}")
    @Operation(
        operationId = "getInitiativeById",
        summary = "Get an initiative by its identifier",
        description = "Returns detailed information about an initiative identified by its external UUID. " +
            "Accessible to users with the 'admin' or 'web-users' role."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Initiative found and returned successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = InitiativeResponse.class)
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
            responseCode = "404",
            description = "Initiative with the given externalId was not found"
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public InitiativeResponse get(
        @Parameter(
            name = "externalId",
            description = "External unique identifier of the initiative (UUID)",
            required = true,
            in = ParameterIn.PATH,
            example = "550e8400-e29b-41d4-a716-446655440000",
            schema = @Schema(type = SchemaType.STRING, format = "uuid")
        )
        @PathParam("externalId") UUID externalId
    ) {
        var initiative = service.get(externalId);
        return mapper.toResponse(initiative);
    }
}
