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
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.UUID;

@Path("/coworking/guests")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "web-users"})
@Tag(
    name = "Coworking Guests",
    description = "Manage coworking guests: fetch guest details, create new guests, and update existing ones"
)
@SecurityRequirement(name = "bearerAuth")
public class CoworkingGuestResource {

    private final CoworkingGuestService service;
    private final ApiCoworkingGuestMapper mapper;

    public CoworkingGuestResource(CoworkingGuestService service, ApiCoworkingGuestMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GET
    @Path("/{externalId}")
    @Operation(
        operationId = "getCoworkingGuestById",
        summary = "Get a coworking guest by its identifier",
        description = "Returns detailed information about a coworking guest identified by their external UUID. " +
            "Accessible to users with the 'admin' or 'web-users' role."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Guest found and returned successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = CoworkingGuestResponse.class)
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
            description = "Guest with the given externalId was not found"
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public CoworkingGuestResponse get(
        @Parameter(
            name = "externalId",
            description = "External unique identifier of the guest (UUID)",
            required = true,
            in = ParameterIn.PATH,
            example = "550e8400-e29b-41d4-a716-446655440000",
            schema = @Schema(type = SchemaType.STRING, format = "uuid")
        )
        @PathParam("externalId") final UUID externalId
    ) {
        var result = service.get(externalId);
        return mapper.toResponse(result);
    }

    @POST
    @Operation(
        operationId = "createCoworkingGuest",
        summary = "Create a new coworking guest",
        description = "Creates a new coworking guest with the provided personal and contact information. " +
            "Returns the created guest together with a Location header pointing to the new resource. " +
            "Accessible to users with the 'admin' or 'web-users' role."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "Guest created successfully",
            headers = @Header(
                name = "Location",
                description = "URI of the newly created guest resource",
                schema = @Schema(type = SchemaType.STRING, format = "uri")
            ),
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = CoworkingGuestResponse.class)
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Request body validation failed (missing or invalid fields)"
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
            responseCode = "409",
            description = "A guest with the same unique attributes (e.g. phone or telegram) already exists"
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public Response create(
        @RequestBody(
            description = "Guest creation payload",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = CreateCoworkingGuestRequest.class)
            )
        )
        @Valid CreateCoworkingGuestRequest req
    ) {
        var created = service.create(req.firstName(), req.lastName(), req.phone(), req.telegram());
        return Response.created(URI.create("/coworking/guests/" + created.externalId()))
            .entity(mapper.toResponse(created))
            .build();
    }

    @PATCH
    @Path("/{externalId}")
    @Operation(
        operationId = "updateCoworkingGuest",
        summary = "Partially update a coworking guest",
        description = "Updates one or more fields of an existing coworking guest. " +
            "Only the fields provided in the request body will be changed; omitted fields remain untouched. " +
            "Accessible to users with the 'admin' or 'web-users' role."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Guest updated successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = CoworkingGuestResponse.class)
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Request body validation failed (invalid field values)"
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
            description = "Guest with the given externalId was not found"
        ),
        @APIResponse(
            responseCode = "409",
            description = "Update conflicts with an existing guest (e.g. duplicate phone or telegram)"
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public CoworkingGuestResponse update(
        @Parameter(
            name = "externalId",
            description = "External unique identifier of the guest (UUID)",
            required = true,
            in = ParameterIn.PATH,
            example = "550e8400-e29b-41d4-a716-446655440000",
            schema = @Schema(type = SchemaType.STRING, format = "uuid")
        )
        @PathParam("externalId") UUID externalId,
        @RequestBody(
            description = "Fields to update. Omitted fields will not be changed.",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = UpdateCoworkingGuestRequest.class)
            )
        )
        @Valid UpdateCoworkingGuestRequest req
    ) {
        var command = mapper.toCommand(req);
        var updated = service.update(externalId, command);
        return mapper.toResponse(updated);
    }
}