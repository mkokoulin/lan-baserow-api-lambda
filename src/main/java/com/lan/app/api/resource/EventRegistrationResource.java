package com.lan.app.api.resource;

import com.lan.app.api.dto.request.EventRegistrationCreateRequest;
import com.lan.app.api.dto.response.EventRegistrationResponse;
import com.lan.app.api.mapper.EventRegistrationMapper;
import com.lan.app.service.EventRegistrationService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;

@Path("/events/registrations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "web-users"})
@Tag(
    name = "Event Registrations",
    description = "Register guests for events"
)
@SecurityRequirement(name = "bearerAuth")
public class EventRegistrationResource {

    private final EventRegistrationService service;
    private final EventRegistrationMapper mapper;
    private final RegistrationConfirmStore confirmStore;

    public EventRegistrationResource(EventRegistrationService service, EventRegistrationMapper mapper,
                                     RegistrationConfirmStore confirmStore) {
        this.service = service;
        this.mapper = mapper;
        this.confirmStore = confirmStore;
    }

    @POST
    @Operation(
        operationId = "createEventRegistration",
        summary = "Register a guest for an event",
        description = "Creates a new event registration linking a guest to an event. " +
            "Accessible to users with the 'admin' or 'web-users' role."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "Registration created successfully",
            headers = @Header(
                name = "Location",
                description = "URI of the newly created registration resource",
                schema = @Schema(type = SchemaType.STRING, format = "uri")
            ),
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = EventRegistrationResponse.class)
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
            responseCode = "404",
            description = "Event or guest with the given identifier was not found"
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public Response create(
        @RequestBody(
            description = "Registration creation payload",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = EventRegistrationCreateRequest.class)
            )
        )
        @Valid EventRegistrationCreateRequest req
    ) {
        var command = mapper.toCommand(req);
        var created = service.create(command);
        return Response.created(URI.create("/events/registrations/" + created.id().externalId()))
            .entity(mapper.toResponse(created))
            .build();
    }

    @POST
    @Path("/{regId}/confirm")
    @PermitAll
    @Operation(operationId = "confirmRegistration", summary = "Mark a site registration as bot-confirmed")
    public Response confirm(@PathParam("regId") String regId) {
        confirmStore.confirm(regId);
        return Response.ok().build();
    }

    @GET
    @Path("/{regId}/confirmed")
    @PermitAll
    @Operation(operationId = "isRegistrationConfirmed", summary = "Check if a registration has been bot-confirmed")
    public Response isConfirmed(@PathParam("regId") String regId) {
        return Response.ok(java.util.Map.of("confirmed", confirmStore.isConfirmed(regId))).build();
    }
}
