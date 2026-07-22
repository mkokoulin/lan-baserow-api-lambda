package com.lan.app.api.resource;

import com.lan.app.api.dto.request.CreateSupportComplaintRequest;
import com.lan.app.api.dto.response.SupportComplaintResponse;
import com.lan.app.api.mapper.ApiSupportComplaintMapper;
import com.lan.app.service.SupportComplaintService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/support/v1/complaints")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "web-users"})
@SecurityRequirement(name = "bearerAuth")
@Tag(
    name = "Support Complaints",
    description = "Store support complaints submitted through the website"
)
public class SupportComplaintResource {

    private final SupportComplaintService service;
    private final ApiSupportComplaintMapper mapper;

    public SupportComplaintResource(SupportComplaintService service, ApiSupportComplaintMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @POST
    @Operation(
        operationId = "createSupportComplaint",
        summary = "Create a support complaint",
        description = "Stores a support complaint submitted through the website widget in Baserow."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "Complaint created successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = SupportComplaintResponse.class)
            )
        ),
        @APIResponse(responseCode = "400", description = "Validation failed"),
        @APIResponse(responseCode = "401", description = "User is not authenticated"),
        @APIResponse(responseCode = "403", description = "User does not have permission"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response create(
        @RequestBody(required = true) @Valid CreateSupportComplaintRequest req
    ) {
        var cmd = mapper.toCommand(req);
        var created = service.create(cmd);
        var response = mapper.toResponse(created);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }
}
