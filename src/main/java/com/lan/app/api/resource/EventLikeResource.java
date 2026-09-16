package com.lan.app.api.resource;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.lan.app.api.dto.request.CreateEventLikeRequest;
import com.lan.app.api.dto.response.EventLikesResponse;
import com.lan.app.service.EventLikeService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/events/v1/{externalId}/likes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "web-users"})
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Event likes", description = "Public like counter per event, deduped per anonymous browser visitor")
public class EventLikeResource {

    private final EventLikeService service;

    public EventLikeResource(EventLikeService service) {
        this.service = service;
    }

    @POST
    @Operation(
        operationId = "likeEvent",
        summary = "Like an event",
        description = "Idempotent: liking twice with the same anonId does not double-count."
    )
    public EventLikesResponse like(
        @PathParam("externalId") UUID externalId,
        @Valid CreateEventLikeRequest request
    ) {
        var count = service.like(externalId, request.anonId());
        return new EventLikesResponse(count);
    }

    @DELETE
    @Operation(
        operationId = "unlikeEvent",
        summary = "Remove a like from an event"
    )
    public EventLikesResponse unlike(
        @PathParam("externalId") UUID externalId,
        @QueryParam("anonId") @NotBlank String anonId
    ) {
        var count = service.unlike(externalId, anonId);
        return new EventLikesResponse(count);
    }
}
