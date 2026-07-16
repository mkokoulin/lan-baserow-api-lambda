package com.lan.app.api.resource;

import com.lan.app.api.dto.request.NotificationActionRequest;
import com.lan.app.api.dto.request.NotificationResultRequest;
import com.lan.app.api.dto.response.BotRegistrationDto;
import com.lan.app.api.dto.response.EventNotificationDueResponse;
import com.lan.app.api.dto.response.RecipientDto;
import com.lan.app.service.EventNotificationService;
import com.lan.app.service.EventRegistrationService;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
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
    private final EventNotificationService notificationService;

    public BotResource(EventRegistrationService service, EventNotificationService notificationService) {
        this.service = service;
        this.notificationService = notificationService;
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

    @GET
    @Path("/event-notifications/due")
    @Operation(
        operationId = "botDueEventNotifications",
        summary = "Return event notifications that are due to be sent now",
        description = "Returns active pending event notifications whose scheduled send time (event start minus lead hours) " +
            "has been reached and the current time is within working hours (09:00–21:00 Yerevan). " +
            "Each returned notification is immediately marked as 'sending' to prevent double-delivery. " +
            "After sending, call mark-sent or mark-failed."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "List of due notifications (may be empty)",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(type = SchemaType.ARRAY, implementation = EventNotificationDueResponse.class)
            )
        )
    })
    public Response dueEventNotifications() {
        var due = notificationService.findDue().stream()
            .map(d -> new EventNotificationDueResponse(
                d.rowId(), d.messageEn(), d.messageRu(), d.eventName(),
                d.recipients().stream()
                    .map(r -> new RecipientDto(r.chatId(), r.guestRowId(), r.registrationRowId()))
                    .toList()
            ))
            .toList();
        return Response.ok(due).build();
    }

    @POST
    @Path("/event-notifications/{id}/mark-sent")
    @Operation(operationId = "botMarkEventNotificationSent", summary = "Mark an event notification as sent")
    public Response markEventNotificationSent(@PathParam("id") int id) {
        notificationService.markSent(id);
        return Response.ok().build();
    }

    @POST
    @Path("/event-notifications/{id}/mark-failed")
    @Operation(operationId = "botMarkEventNotificationFailed", summary = "Mark an event notification as failed")
    public Response markEventNotificationFailed(@PathParam("id") int id) {
        notificationService.markFailed(id);
        return Response.ok().build();
    }

    @POST
    @Path("/event-notifications/{id}/results")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(operationId = "botSaveEventNotificationResults", summary = "Save per-guest notification results")
    public Response saveEventNotificationResults(
        @PathParam("id") int id,
        List<NotificationResultRequest> results
    ) {
        if (results == null || results.isEmpty()) return Response.ok().build();
        notificationService.saveResults(id, results);
        return Response.ok().build();
    }

    @POST
    @Path("/event-notifications/{id}/action")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "botRecordEventNotificationAction",
        summary = "Record a guest's attendance answer for a reminder",
        description = "Called when the guest taps the 'Всё в силе' / 'Не смогу' button on a reminder. " +
            "Stores the answer (CONFIRMED/DECLINED) on the guest's notification-result row."
    )
    public Response recordEventNotificationAction(
        @PathParam("id") int id,
        NotificationActionRequest req
    ) {
        if (req == null || req.action() == null || req.action().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\":\"action is required\"}")
                .build();
        }
        notificationService.recordGuestAction(id, req.guestRowId(), req.registrationRowId(), req.action());
        return Response.ok().build();
    }
}
