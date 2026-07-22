package com.lan.app.infrastructure.baserow.client;

import com.baserow.client.BaserowAuthHeaders;
import com.baserow.dto.BaserowListResponse;
import com.lan.app.infrastructure.baserow.dto.BaserowEventNotificationRow;
import com.lan.app.infrastructure.baserow.dto.CreateEventNotificationRowRequest;
import com.lan.app.infrastructure.baserow.dto.UpdateEventNotificationStatusRequest;
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
public interface BaserowEventNotificationClient {

    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    @ClientQueryParam(name = "size", value = "100")
    @ClientQueryParam(name = "filter__active__equal", value = "true")
    BaserowListResponse<BaserowEventNotificationRow> listActive(
        @PathParam("tableId") int tableId
    );

    // Finds the anchor row for a given event, if one was already created (lazily, on first send).
    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    @ClientQueryParam(name = "size", value = "1")
    BaserowListResponse<BaserowEventNotificationRow> findByEventIdRaw(
        @PathParam("tableId") int tableId,
        @QueryParam("filter__event_id__link_row_has") int eventRowId
    );

    @POST
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    BaserowEventNotificationRow create(
        @PathParam("tableId") int tableId,
        CreateEventNotificationRowRequest body
    );

    @PATCH
    @Path("/{tableId}/{rowId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    BaserowEventNotificationRow updateStatus(
        @PathParam("tableId") int tableId,
        @PathParam("rowId") int rowId,
        UpdateEventNotificationStatusRequest body
    );
}
