package com.lan.app.api.resource;

import com.lan.app.api.dto.response.BotRegistrationDto;
import com.lan.app.service.EventRegistrationService;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/events/v1/bot")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
@Tag(name = "Bot", description = "Internal endpoints for the Telegram bot")
public class BotResource {

    private final EventRegistrationService service;

    public BotResource(EventRegistrationService service) {
        this.service = service;
    }

    @GET
    @Path("/my-registrations")
    @Operation(
        operationId = "botMyRegistrations",
        summary = "Return events a Telegram user has registered for",
        description = "Looks up event registrations by Telegram chat ID and returns the list of events. " +
            "Returns an empty array if the user has no registrations."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "List of event registrations",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(type = SchemaType.ARRAY, implementation = BotRegistrationDto.class)
            )
        ),
        @APIResponse(responseCode = "400", description = "chatId query parameter is missing or invalid")
    })
    public Response myRegistrations(@QueryParam("chatId") Long chatId) {
        if (chatId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"chatId is required\"}")
                    .build();
        }

        List<BotRegistrationDto> result = service.findByChatId(chatId).stream()
                .map(item -> new BotRegistrationDto(item.eventName(), item.dateStart()))
                .toList();

        return Response.ok(result).build();
    }
}
