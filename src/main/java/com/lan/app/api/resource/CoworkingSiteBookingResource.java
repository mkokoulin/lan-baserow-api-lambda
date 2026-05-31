package com.lan.app.api.resource;

import com.lan.app.api.dto.request.CreateCoworkingSiteBookingRequest;
import com.lan.app.api.dto.response.CoworkingSiteBookingResponse;
import com.lan.app.api.mapper.ApiCoworkingSiteBookingMapper;
import com.lan.app.service.CoworkingSiteBookingService;
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

@Path("/coworking/v1/site-bookings")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "web-users"})
@SecurityRequirement(name = "bearerAuth")
@Tag(
    name = "Coworking Site Bookings",
    description = "Store coworking bookings submitted through the website"
)
public class CoworkingSiteBookingResource {

    private final CoworkingSiteBookingService service;
    private final ApiCoworkingSiteBookingMapper mapper;

    public CoworkingSiteBookingResource(CoworkingSiteBookingService service, ApiCoworkingSiteBookingMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @POST
    @Operation(
        operationId = "createCoworkingSiteBooking",
        summary = "Create a coworking site booking",
        description = "Stores a coworking booking submitted through the website form in Baserow."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "Booking created successfully",
            headers = @Header(
                name = "Location",
                description = "URI of the newly created booking",
                schema = @Schema(type = SchemaType.STRING, format = "uri")
            ),
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = CoworkingSiteBookingResponse.class)
            )
        ),
        @APIResponse(responseCode = "400", description = "Validation failed"),
        @APIResponse(responseCode = "401", description = "User is not authenticated"),
        @APIResponse(responseCode = "403", description = "User does not have permission"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response create(
        @RequestBody(required = true) @Valid CreateCoworkingSiteBookingRequest req
    ) {
        var cmd = mapper.toCommand(req);
        var created = service.create(cmd);
        var response = mapper.toResponse(created);
        return Response.created(URI.create("/coworking/v1/site-bookings/" + created.externalId()))
            .entity(response)
            .build();
    }
}
