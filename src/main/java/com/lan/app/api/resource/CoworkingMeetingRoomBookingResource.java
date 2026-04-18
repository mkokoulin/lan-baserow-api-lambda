package com.lan.app.api.resource;

import com.lan.app.api.dto.request.CreateCoworkingMeetingRoomBookingRequest;
import com.lan.app.api.dto.request.UpdateCoworkingMeetingRoomBookingRequest;
import com.lan.app.api.dto.response.CoworkingMeetingRoomBookingResponse;
import com.lan.app.api.mapper.ApiCoworkingMeetingRoomBookingMapper;
import com.lan.app.service.CoworkingMeetingRoomBookingService;
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

@Path("/coworking/meeting-room-booking")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "web-users"})
@Tag(
    name = "Coworking Meeting Room Bookings",
    description = "Manage meeting room bookings: fetch booking details, create new bookings, and update existing ones"
)
@SecurityRequirement(name = "bearerAuth")
public class CoworkingMeetingRoomBookingResource {

    private final CoworkingMeetingRoomBookingService service;
    private final ApiCoworkingMeetingRoomBookingMapper mapper;

    public CoworkingMeetingRoomBookingResource(
        CoworkingMeetingRoomBookingService service,
        ApiCoworkingMeetingRoomBookingMapper mapper
    ) {
        this.service = service;
        this.mapper = mapper;
    }

    @GET
    @Path("/{externalId}")
    @Operation(
        operationId = "getCoworkingMeetingRoomBookingById",
        summary = "Get a meeting room booking by its identifier",
        description = "Returns detailed information about a meeting room booking identified by its external UUID. " +
            "Accessible to users with the 'admin' or 'web-users' role."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Booking found and returned successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = CoworkingMeetingRoomBookingResponse.class)
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
            description = "Booking with the given externalId was not found"
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public CoworkingMeetingRoomBookingResponse get(
        @Parameter(
            name = "externalId",
            description = "External unique identifier of the meeting room booking (UUID)",
            required = true,
            in = ParameterIn.PATH,
            example = "550e8400-e29b-41d4-a716-446655440000",
            schema = @Schema(type = SchemaType.STRING, format = "uuid")
        )
        @PathParam("externalId") UUID externalId
    ) {
        var meetingRoomBooking = service.get(externalId);
        return mapper.toResponse(meetingRoomBooking);
    }

    @POST
    @Operation(
        operationId = "createCoworkingMeetingRoomBooking",
        summary = "Create a new meeting room booking",
        description = "Creates a new meeting room booking for the given time slot and guest. " +
            "Returns the created booking together with a Location header pointing to the new resource. " +
            "Accessible to users with the 'admin' or 'web-users' role."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "Booking created successfully",
            headers = @Header(
                name = "Location",
                description = "URI of the newly created booking resource",
                schema = @Schema(type = SchemaType.STRING, format = "uri")
            ),
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = CoworkingMeetingRoomBookingResponse.class)
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Request body validation failed (missing or invalid fields, e.g. end date before start date)"
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
            description = "Referenced entity (e.g. guest or meeting room) was not found"
        ),
        @APIResponse(
            responseCode = "409",
            description = "The requested time slot conflicts with an existing booking"
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public Response create(
        @RequestBody(
            description = "Meeting room booking creation payload",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = CreateCoworkingMeetingRoomBookingRequest.class)
            )
        )
        @Valid CreateCoworkingMeetingRoomBookingRequest req
    ) {
        var command = mapper.toCommand(req);
        var created = service.create(command);
        return Response.created(URI.create("/coworking/meeting-room-booking/" + created.externalId()))
            .entity(mapper.toResponse(created))
            .build();
    }

    @PATCH
    @Path("/{externalId}")
    @Operation(
        operationId = "updateCoworkingMeetingRoomBooking",
        summary = "Partially update a meeting room booking",
        description = "Updates one or more fields of an existing meeting room booking. " +
            "Only the fields provided in the request body will be changed; omitted fields remain untouched. " +
            "Accessible to users with the 'admin' or 'web-users' role."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Booking updated successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = CoworkingMeetingRoomBookingResponse.class)
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
            description = "Booking with the given externalId was not found"
        ),
        @APIResponse(
            responseCode = "409",
            description = "The updated time slot conflicts with an existing booking"
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public CoworkingMeetingRoomBookingResponse update(
        @Parameter(
            name = "externalId",
            description = "External unique identifier of the booking to update (UUID)",
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
                schema = @Schema(implementation = UpdateCoworkingMeetingRoomBookingRequest.class)
            )
        )
        @Valid UpdateCoworkingMeetingRoomBookingRequest req
    ) {
        var command = mapper.toCommand(req);
        var updated = service.update(externalId, command);
        return mapper.toResponse(updated);
    }
}