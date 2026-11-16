package com.lan.app.api.resource;

import com.lan.app.api.dto.request.CreateBirthdayWishRequest;
import com.lan.app.api.dto.response.BirthdayWishResponse;
import com.lan.app.api.mapper.ApiBirthdayWishMapper;
import com.lan.app.service.BirthdayWishService;
import com.lan.app.service.command.CreateBirthdayWishCommand;
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
import java.util.List;

@Path("/coworking/v1/birthday-wishes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(
    name = "Birthday wishes",
    description = "Submit a birthday wish for LAN's anniversary"
)
public class CoworkingBirthdayWishResource {

    private final BirthdayWishService service;
    private final ApiBirthdayWishMapper mapper;

    public CoworkingBirthdayWishResource(BirthdayWishService service, ApiBirthdayWishMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GET
    @PermitAll
    @Operation(
        operationId = "listBirthdayWishes",
        summary = "List all birthday wishes",
        description = "Returns all submitted birthday wishes ordered by date descending. Accessible to everyone."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Birthday wishes retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = BirthdayWishResponse.class, type = SchemaType.ARRAY)
            )
        ),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public List<BirthdayWishResponse> list() {
        return service.list().stream().map(mapper::toResponse).toList();
    }

    @POST
    @RolesAllowed({"admin", "web-users"})
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        operationId = "createBirthdayWish",
        summary = "Submit a birthday wish",
        description = "Creates a new birthday wish. Accessible to users with the 'admin' or 'web-users' role."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "Birthday wish created successfully",
            headers = @Header(
                name = "Location",
                description = "URI of the newly created birthday wish",
                schema = @Schema(type = SchemaType.STRING, format = "uri")
            ),
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = BirthdayWishResponse.class)
            )
        ),
        @APIResponse(responseCode = "400", description = "Validation failed"),
        @APIResponse(responseCode = "401", description = "User is not authenticated"),
        @APIResponse(responseCode = "403", description = "User does not have permission"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response create(
        @RequestBody(required = true) @Valid CreateBirthdayWishRequest request
    ) {
        var created = service.create(new CreateBirthdayWishCommand(
            request.authorName(),
            request.telegramUserId(),
            request.text()
        ));
        var response = mapper.toResponse(created);
        return Response.created(URI.create("/v1/birthday-wishes/" + created.id()))
            .entity(response)
            .build();
    }
}
