package com.lan.app.api.resource;

import com.lan.app.api.dto.response.CoworkingGuestResponse;
import com.lan.app.api.dto.response.LinkStatusResponse;
import com.lan.app.api.dto.request.CreateCoworkingGuestRequest;
import com.lan.app.api.dto.request.LinkCoworkingGuestChatByIdRequest;
import com.lan.app.api.dto.request.LinkCoworkingGuestChatRequest;
import com.lan.app.api.dto.request.UpdateCoworkingGuestRequest;
import com.lan.app.api.mapper.ApiCoworkingGuestMapper;
import com.lan.app.service.CoworkingGuestService;
import com.lan.app.service.LinkSessionService;
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
import java.util.Optional;

@Path("/coworking/v1/guests")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "web-users"})
@Tag(
    name = "Coworking Guests",
    description = "Manage coworking guests: fetch guest details, create new guests, and update existing ones"
)
@SecurityRequirement(name = "bearerAuth")
public class CoworkingGuestResource {

    private final CoworkingGuestService service;
    private final ApiCoworkingGuestMapper mapper;
    private final LinkSessionService linkSessionService;

    public CoworkingGuestResource(CoworkingGuestService service, ApiCoworkingGuestMapper mapper, LinkSessionService linkSessionService) {
        this.service = service;
        this.mapper = mapper;
        this.linkSessionService = linkSessionService;
    }

    @GET
    @Operation(
        operationId = "getCoworkingGuestByChatId",
        summary = "Find a coworking guest by Telegram chat ID",
        description = "Returns the coworking guest whose telegramChatId matches the given value. " +
            "Returns 404 if no such guest exists."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Guest found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = CoworkingGuestResponse.class)
            )
        ),
        @APIResponse(responseCode = "400", description = "chatId query parameter is missing"),
        @APIResponse(responseCode = "401", description = "User is not authenticated"),
        @APIResponse(responseCode = "403", description = "User does not have permission"),
        @APIResponse(responseCode = "404", description = "Guest not found"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getByChatId(
        @Parameter(name = "chatId", description = "Telegram chat ID of the guest", in = ParameterIn.QUERY)
        @QueryParam("chatId") Long chatId
    ) {
        if (chatId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\":\"chatId is required\"}")
                .build();
        }
        return service.findByChatId(chatId)
            .map(g -> Response.ok(mapper.toResponse(g)).build())
            .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/{externalId}")
    @Operation(
        operationId = "getCoworkingGuestById",
        summary = "Get a coworking guest by its identifier",
        description = "Returns detailed information about a coworking guest identified by their external UUID. " +
            "Accessible to users with the 'admin' or 'web-users' role."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Guest found and returned successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = CoworkingGuestResponse.class)
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
            description = "Guest with the given externalId was not found"
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public CoworkingGuestResponse get(
        @Parameter(
            name = "externalId",
            description = "External unique identifier of the guest (UUID)",
            required = true,
            in = ParameterIn.PATH,
            example = "550e8400-e29b-41d4-a716-446655440000",
            schema = @Schema(type = SchemaType.STRING, format = "uuid")
        )
        @PathParam("externalId") final UUID externalId
    ) {
        var result = service.get(externalId);
        return mapper.toResponse(result);
    }

    @POST
    @Operation(
        operationId = "createCoworkingGuest",
        summary = "Create a new coworking guest",
        description = "Creates a new coworking guest with the provided personal and contact information. " +
            "Returns the created guest together with a Location header pointing to the new resource. " +
            "Accessible to users with the 'admin' or 'web-users' role."
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
                schema = @Schema(implementation = CoworkingGuestResponse.class)
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
            responseCode = "409",
            description = "A guest with the same unique attributes (e.g. phone or telegram) already exists"
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public Response create(
        @RequestBody(
            description = "Guest creation payload",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = CreateCoworkingGuestRequest.class)
            )
        )
        @Valid CreateCoworkingGuestRequest req
    ) {
        Long chatId = req.telegramChatId() != null ? Long.parseLong(req.telegramChatId()) : null;
        var created = service.create(req.firstName(), req.lastName(), req.phone(), req.telegram(), chatId);
        return Response.created(URI.create("/coworking/guests/" + created.externalId()))
            .entity(mapper.toResponse(created))
            .build();
    }

    @GET
    @Path("/by-phone")
    @Operation(
        operationId = "getCoworkingGuestByPhone",
        summary = "Find a coworking guest by phone number",
        description = "Returns the coworking guest whose phone matches the given value. Returns 404 if no such guest exists."
    )
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Guest found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = CoworkingGuestResponse.class))),
        @APIResponse(responseCode = "400", description = "phone query parameter is missing"),
        @APIResponse(responseCode = "401", description = "User is not authenticated"),
        @APIResponse(responseCode = "403", description = "User does not have permission"),
        @APIResponse(responseCode = "404", description = "Guest not found"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getByPhone(
        @Parameter(name = "phone", description = "Phone number of the guest", in = ParameterIn.QUERY)
        @QueryParam("phone") String phone
    ) {
        if (phone == null || phone.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\":\"phone is required\"}")
                .build();
        }
        return service.findByPhone(phone)
            .map(g -> Response.ok(mapper.toResponse(g)).build())
            .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @PATCH
    @Path("/{externalId}/link-chat")
    @Operation(
        operationId = "linkCoworkingGuestChatById",
        summary = "Link a Telegram chat ID to a guest by UUID",
        description = "Sets the given Telegram chat ID on the guest identified by their external UUID."
    )
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Chat ID linked successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = CoworkingGuestResponse.class))),
        @APIResponse(responseCode = "400", description = "Validation failed"),
        @APIResponse(responseCode = "401", description = "User is not authenticated"),
        @APIResponse(responseCode = "403", description = "User does not have permission"),
        @APIResponse(responseCode = "404", description = "Guest not found"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public CoworkingGuestResponse linkChatById(
        @Parameter(name = "externalId", description = "External UUID of the guest",
            required = true, in = ParameterIn.PATH,
            schema = @Schema(type = SchemaType.STRING, format = "uuid"))
        @PathParam("externalId") UUID externalId,
        @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = LinkCoworkingGuestChatByIdRequest.class)))
        @Valid LinkCoworkingGuestChatByIdRequest req
    ) {
        return mapper.toResponse(service.linkChatIdById(externalId, req.chatId()));
    }

    @POST
    @Path("/link-chat")
    @Operation(
        operationId = "linkCoworkingGuestChat",
        summary = "Link a Telegram chat ID to a guest by phone number",
        description = "Finds a coworking guest by their phone number and sets the given Telegram chat ID on their record. " +
            "Returns 404 if no guest with that phone is found."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Chat ID linked successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = CoworkingGuestResponse.class)
            )
        ),
        @APIResponse(responseCode = "400", description = "Request body validation failed"),
        @APIResponse(responseCode = "401", description = "User is not authenticated"),
        @APIResponse(responseCode = "403", description = "User does not have permission"),
        @APIResponse(responseCode = "404", description = "Guest with the given phone not found"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response linkChat(
        @RequestBody(required = true, content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = LinkCoworkingGuestChatRequest.class)
        ))
        @Valid LinkCoworkingGuestChatRequest req
    ) {
        return service.linkChatIdByPhone(req.phone(), req.chatId())
            .map(g -> Response.ok(mapper.toResponse(g)).build())
            .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/{externalId}/link-status")
    @Operation(
        operationId = "getCoworkingGuestLinkStatus",
        summary = "Get the Telegram link session status for a guest",
        description = "Returns whether the current link session is confirmed, rejected, or still pending. " +
            "Used by the frontend to poll until the guest confirms their Telegram account."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Link status returned",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = LinkStatusResponse.class))
        ),
        @APIResponse(responseCode = "401", description = "User is not authenticated"),
        @APIResponse(responseCode = "403", description = "User does not have permission"),
        @APIResponse(responseCode = "404", description = "Guest not found"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getLinkStatus(
        @Parameter(name = "externalId", description = "External UUID of the guest",
            required = true, in = ParameterIn.PATH,
            schema = @Schema(type = SchemaType.STRING, format = "uuid"))
        @PathParam("externalId") UUID externalId
    ) {
        service.get(externalId);
        var status = linkSessionService.getStatus(externalId);
        return Response.ok(new LinkStatusResponse(
            status == LinkSessionService.LinkStatus.CONFIRMED,
            status == LinkSessionService.LinkStatus.REJECTED
        )).build();
    }

    @POST
    @Path("/{externalId}/link-init")
    @Operation(
        operationId = "initCoworkingGuestLinkSession",
        summary = "Initialize a Telegram link session for a guest",
        description = "Resets the link session to PENDING. Call this when signup or login is initiated " +
            "so the guest must re-confirm via Telegram."
    )
    @APIResponses({
        @APIResponse(responseCode = "204", description = "Session initialized"),
        @APIResponse(responseCode = "401", description = "User is not authenticated"),
        @APIResponse(responseCode = "403", description = "User does not have permission"),
        @APIResponse(responseCode = "404", description = "Guest not found"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response initLinkSession(
        @Parameter(name = "externalId", description = "External UUID of the guest",
            required = true, in = ParameterIn.PATH,
            schema = @Schema(type = SchemaType.STRING, format = "uuid"))
        @PathParam("externalId") UUID externalId
    ) {
        service.get(externalId);
        linkSessionService.init(externalId);
        return Response.noContent().build();
    }

    @POST
    @Path("/{externalId}/link-confirm")
    @Operation(
        operationId = "confirmCoworkingGuestLink",
        summary = "Confirm the Telegram link session for a guest",
        description = "Marks the current link session as CONFIRMED. Called by the Telegram bot webhook " +
            "when the guest opens the bot and their account is verified."
    )
    @APIResponses({
        @APIResponse(responseCode = "204", description = "Session confirmed"),
        @APIResponse(responseCode = "401", description = "User is not authenticated"),
        @APIResponse(responseCode = "403", description = "User does not have permission"),
        @APIResponse(responseCode = "404", description = "Guest not found"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response confirmLink(
        @Parameter(name = "externalId", description = "External UUID of the guest",
            required = true, in = ParameterIn.PATH,
            schema = @Schema(type = SchemaType.STRING, format = "uuid"))
        @PathParam("externalId") UUID externalId
    ) {
        service.get(externalId);
        linkSessionService.confirm(externalId);
        return Response.noContent().build();
    }

    @POST
    @Path("/{externalId}/link-reject")
    @Operation(
        operationId = "rejectCoworkingGuestLink",
        summary = "Reject the Telegram link session for a guest",
        description = "Marks the current link session as REJECTED. Called by the Telegram bot webhook " +
            "when a different Telegram account attempts to log in as this guest."
    )
    @APIResponses({
        @APIResponse(responseCode = "204", description = "Session rejected"),
        @APIResponse(responseCode = "401", description = "User is not authenticated"),
        @APIResponse(responseCode = "403", description = "User does not have permission"),
        @APIResponse(responseCode = "404", description = "Guest not found"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response rejectLink(
        @Parameter(name = "externalId", description = "External UUID of the guest",
            required = true, in = ParameterIn.PATH,
            schema = @Schema(type = SchemaType.STRING, format = "uuid"))
        @PathParam("externalId") UUID externalId
    ) {
        service.get(externalId);
        linkSessionService.reject(externalId);
        return Response.noContent().build();
    }

    @PATCH
    @Path("/{externalId}")
    @Operation(
        operationId = "updateCoworkingGuest",
        summary = "Partially update a coworking guest",
        description = "Updates one or more fields of an existing coworking guest. " +
            "Only the fields provided in the request body will be changed; omitted fields remain untouched. " +
            "Accessible to users with the 'admin' or 'web-users' role."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Guest updated successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = CoworkingGuestResponse.class)
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
            description = "Guest with the given externalId was not found"
        ),
        @APIResponse(
            responseCode = "409",
            description = "Update conflicts with an existing guest (e.g. duplicate phone or telegram)"
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public CoworkingGuestResponse update(
        @Parameter(
            name = "externalId",
            description = "External unique identifier of the guest (UUID)",
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
                schema = @Schema(implementation = UpdateCoworkingGuestRequest.class)
            )
        )
        @Valid UpdateCoworkingGuestRequest req
    ) {
        var command = mapper.toCommand(req);
        var updated = service.update(externalId, command);
        return mapper.toResponse(updated);
    }
}