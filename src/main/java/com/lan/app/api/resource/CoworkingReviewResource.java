package com.lan.app.api.resource;

import com.lan.app.api.dto.request.CreateReviewRequest;
import com.lan.app.api.dto.response.ReviewResponse;
import com.lan.app.api.mapper.ApiReviewMapper;
import com.lan.app.service.ReviewService;
import com.lan.app.service.command.CreateReviewCommand;
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

@Path("/coworking/v1/reviews")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(
    name = "Reviews",
    description = "Submit and retrieve reviews"
)
public class CoworkingReviewResource {

    private final ReviewService service;
    private final ApiReviewMapper mapper;

    public CoworkingReviewResource(ReviewService service, ApiReviewMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GET
    @PermitAll
    @Operation(
        operationId = "listReviews",
        summary = "List all reviews",
        description = "Returns all submitted reviews ordered by date descending. Accessible to everyone."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Reviews retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = ReviewResponse.class, type = SchemaType.ARRAY)
            )
        ),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public List<ReviewResponse> list() {
        return service.list().stream().map(mapper::toResponse).toList();
    }

    @POST
    @RolesAllowed({"admin", "web-users"})
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        operationId = "createReview",
        summary = "Submit a review",
        description = "Creates a new review. Accessible to users with the 'admin' or 'web-users' role."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "Review created successfully",
            headers = @Header(
                name = "Location",
                description = "URI of the newly created review",
                schema = @Schema(type = SchemaType.STRING, format = "uri")
            ),
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = ReviewResponse.class)
            )
        ),
        @APIResponse(responseCode = "400", description = "Validation failed"),
        @APIResponse(responseCode = "401", description = "User is not authenticated"),
        @APIResponse(responseCode = "403", description = "User does not have permission"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response create(
        @RequestBody(required = true) @Valid CreateReviewRequest request
    ) {
        var created = service.create(new CreateReviewCommand(
            request.authorName(),
            request.rating(),
            request.text()
        ));
        var response = mapper.toResponse(created);
        return Response.created(URI.create("/v1/reviews/" + created.id()))
            .entity(response)
            .build();
    }
}
