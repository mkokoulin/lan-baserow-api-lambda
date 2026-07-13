package com.lan.app.infrastructure.baserow.client;

import com.baserow.client.BaserowAuthHeaders;
import com.baserow.dto.BaserowListResponse;
import com.lan.app.infrastructure.baserow.dto.BaserowEventNotificationResultRow;
import com.lan.app.infrastructure.baserow.dto.CreateEventNotificationResultRowRequest;
import com.lan.app.infrastructure.baserow.dto.UpdateNotificationResultActionRequest;
import io.quarkus.rest.client.reactive.ClientQueryParam;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "baserow")
@RegisterProvider(BaserowAuthHeaders.class)
@Path("/api/database/rows/table")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface BaserowEventNotificationResultClient {

    @POST
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    void create(
        @PathParam("tableId") int tableId,
        CreateEventNotificationResultRowRequest body
    );

    // Finds the result row(s) created for a given (event_notification, guest) pair —
    // used to attach the guest's attendance answer to the row created when the reminder was sent.
    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    @ClientQueryParam(name = "size", value = "5")
    BaserowListResponse<BaserowEventNotificationResultRow> findByNotificationAndGuestRaw(
        @PathParam("tableId") int tableId,
        @QueryParam("filter__event_notification__link_row_has") int eventNotificationRowId,
        @QueryParam("filter__guest__link_row_has") int guestRowId
    );

    @PATCH
    @Path("/{tableId}/{rowId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    BaserowEventNotificationResultRow updateAction(
        @PathParam("tableId") int tableId,
        @PathParam("rowId") int rowId,
        UpdateNotificationResultActionRequest body
    );
}
