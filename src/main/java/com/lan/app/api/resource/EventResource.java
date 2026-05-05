package com.lan.app.api.resource;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.lan.app.api.dto.response.EventResponse;
import com.lan.app.api.mapper.ApiEventMapper;
import com.lan.app.service.EventService;
import jakarta.ws.rs.core.MediaType;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;


@Path("/events")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "web-users"})
@Tag(
    name = "Events",
    description = ""
)
@SecurityRequirement(name = "bearerAuth")
public class EventResource {

    private final EventService service;
    private final ApiEventMapper mapper;

    public EventResource(
        EventService service,
        ApiEventMapper mapper
    ) {
        this.service = service;
        this.mapper = mapper;
    }

    
    @GET
    @Operation(
        operationId = "listEvents",
        summary = "",
        description = ""
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Active events retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(
                    implementation = EventResponse.class,
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
    public List<EventResponse> list() {
        var tariffs = service.list();
        return tariffs.stream()
            .map(mapper::toResponse)
            .toList();
    }

    @GET
    @Path("/{externalId}")
    @Operation(
        operationId = "getEventById",
        summary = "",
        description = ""
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Active event found and returned successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = EventResponse.class)
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
            description = "Active event with the given externalId was not found"
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public EventResponse get(
        UUID externalId
    ) {
            var event = service.get(externalId);
            return mapper.toResponse(event);
    }
}
