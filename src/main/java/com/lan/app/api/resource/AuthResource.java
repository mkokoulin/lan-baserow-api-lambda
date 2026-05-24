package com.lan.app.api.resource;

import com.lan.app.api.dto.request.GuestLoginRequest;
import com.lan.app.api.dto.response.ErrorResponse;
import com.lan.app.api.dto.response.GuestLoginResponse;
import com.lan.app.api.exception.ErrorCode;
import com.lan.app.infrastructure.security.jwt.GuestTokenService;
import com.lan.app.service.CoworkingGuestService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Map;

@Path("/auth/v1")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
@Tag(name = "Auth", description = "Guest authentication")
public class AuthResource {

    private final CoworkingGuestService guestService;
    private final GuestTokenService tokenService;

    public AuthResource(CoworkingGuestService guestService, GuestTokenService tokenService) {
        this.guestService = guestService;
        this.tokenService = tokenService;
    }

    @POST
    @Path("/login")
    @Operation(
        operationId = "guestLogin",
        summary = "Authenticate a guest by phone and Telegram chat ID",
        description = """
            Looks up a guest by phone number and verifies that the provided Telegram chat ID matches
            the one stored in the database (set when the guest linked their Telegram account).

            - 200: authentication successful, JWT returned.
            - 404 / GUEST_NOT_FOUND: no guest with this phone — frontend should offer registration.
            - 401 / CHAT_ID_MISMATCH: phone found but chatId does not match — wrong Telegram account.
            """
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = GuestLoginResponse.class))
        ),
        @APIResponse(
            responseCode = "400",
            description = "Request body validation failed"
        ),
        @APIResponse(
            responseCode = "401",
            description = "Telegram chat ID does not match the stored value",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = ErrorResponse.class))
        ),
        @APIResponse(
            responseCode = "404",
            description = "No guest found with this phone number",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response login(
        @RequestBody(required = true,
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = GuestLoginRequest.class)))
        @Valid GuestLoginRequest req
    ) {
        var guest = guestService.findByPhone(req.phone()).orElse(null);

        if (guest == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse(
                    ErrorCode.GUEST_NOT_FOUND.name(),
                    "No guest found with this phone number",
                    Map.of("phone", req.phone())
                ))
                .build();
        }

        if (guest.telegramChatId() == null || !guest.telegramChatId().equals(req.chatId())) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(new ErrorResponse(
                    ErrorCode.CHAT_ID_MISMATCH.name(),
                    "Telegram account does not match",
                    Map.of()
                ))
                .build();
        }

        var token = tokenService.generateToken(guest.externalId());
        return Response.ok(new GuestLoginResponse(token)).build();
    }
}
