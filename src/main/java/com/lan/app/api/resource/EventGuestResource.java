package com.lan.app.api.resource;

import com.lan.app.api.dto.request.CreateEventGuestRequest;
import com.lan.app.api.dto.response.EventGuestResponse;
import com.lan.app.api.mapper.ApiEventGuestMapper;
import com.lan.app.service.EventGuestService;
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

@Path("/events/v1/guests")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "web-users"})
@Tag(
    name = "Event Guests",
    description = "Manage event guests"
)
@SecurityRequirement(name = "bearerAuth")
public class EventGuestResource {

    private final EventGuestService service;
    private final ApiEventGuestMapper mapper;

    public EventGuestResource(EventGuestService service, ApiEventGuestMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @POST
    @Operation(
        operationId = "createEventGuest",
        summary = "Create a new event guest",
        description = "Creates a new event guest. Accessible to users with the 'admin' or 'web-users' role."
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
                schema = @Schema(implementation = EventGuestResponse.class)
            )
        ),
        @APIResponse(responseCode = "400", description = "Validation failed"),
        @APIResponse(responseCode = "401", description = "User is not authenticated"),
        @APIResponse(responseCode = "403", description = "Insufficient permissions"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response create(
        @RequestBody(
            description = "Guest creation payload",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = CreateEventGuestRequest.class)
            )
        )
        @Valid CreateEventGuestRequest req
    ) {
        var created = service.create(
            req.firstName(),
            req.lastName(),
            req.phone(),
            req.telegram(),
            req.source(),
            req.chatId()
        );
        return Response.created(URI.create("/events/guests/" + created.id().externalId()))
            .entity(mapper.toResponse(created))
            .build();
    }
}
