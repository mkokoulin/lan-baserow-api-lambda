package com.lan.app.api.resource;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/secured")
@Produces(MediaType.TEXT_PLAIN)
@Tag(
    name = "Secured",
    description = "Diagnostic endpoints for verifying authentication and authorization behavior"
)
public class SecuredResource {

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/public")
    @PermitAll
    @Operation(
        operationId = "securedPublicEndpoint",
        summary = "Public diagnostic endpoint",
        description = "Returns a static 'public ok' response. Accessible to anyone — no authentication required. " +
            "Useful for verifying that the service is reachable."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Service is reachable",
            content = @Content(
                mediaType = MediaType.TEXT_PLAIN,
                schema = @Schema(type = SchemaType.STRING, examples = "public ok")
            )
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public String publicEndpoint() {
        return "public ok";
    }

    @GET
    @Path("/user")
    @RolesAllowed({"admin", "web-users"})
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        operationId = "securedUserEndpoint",
        summary = "Authenticated diagnostic endpoint",
        description = "Returns a greeting containing the authenticated user's name and the list of their groups " +
            "extracted from the JWT. Useful for verifying that the Bearer token is parsed correctly and that " +
            "the caller has the expected roles. Accessible to users with the 'admin' or 'web-users' role."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Authenticated greeting returned successfully",
            content = @Content(
                mediaType = MediaType.TEXT_PLAIN,
                schema = @Schema(
                    type = SchemaType.STRING,
                    examples = "hello ivan.petrov, groups=[admin, web-users]"
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
    public String userEndpoint() {
        return "hello " + jwt.getName() + ", groups=" + jwt.getGroups();
    }
}