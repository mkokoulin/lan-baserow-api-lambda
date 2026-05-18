package com.lan.app.infrastructure.baserow.client;

import com.lan.app.infrastructure.baserow.dto.BaserowCoworkingMeetingRoomBookingRow;
import com.lan.app.infrastructure.baserow.dto.BaserowListResponse;
import com.lan.app.infrastructure.baserow.dto.CreateCoworkingMeetingRoomBookingRowRequest;
import com.lan.app.infrastructure.baserow.dto.UpdateCoworkingMeetingRoomBookingRowRequest;
import com.lan.app.infrastructure.baserow.exception.BaserowDataIntegrityException;
import com.lan.app.infrastructure.baserow.exception.BaserowNotFoundException;
import io.quarkus.rest.client.reactive.ClientQueryParam;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.UUID;

@RegisterRestClient(configKey = "baserow")
@RegisterProvider(BaserowAuthHeaders.class)
@Path("/api/database/rows/table")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface BaserowCoworkingMeetingRoomBookingClient {

    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    BaserowListResponse<BaserowCoworkingMeetingRoomBookingRow> list(
        @PathParam("tableId") int tableId
    );

    @GET
    @Path("/{tableId}/{rowId}/")
    @ClientQueryParam(name = "size", value = "1")
    @ClientQueryParam(name = "user_field_names", value = "true")
    BaserowCoworkingMeetingRoomBookingRow getByRowId(
        @PathParam("tableId") int tableId,
        @PathParam("rowId") int rowId
    );

    @GET
    @ClientQueryParam(name = "user_field_names", value = "true")
    @ClientQueryParam(name = "size", value = "1")
    @Path("/{tableId}/")
    BaserowListResponse<BaserowCoworkingMeetingRoomBookingRow> findByExternalIdRaw(
        @PathParam("tableId") int tableId,
        @QueryParam("filter__field_externalId__equal") UUID externalId
    );

    default BaserowCoworkingMeetingRoomBookingRow findUniqueByExternalId(int tableId, UUID externalId) {
        var resp = findByExternalIdRaw(tableId, externalId);

        if (resp.count() == 0 || resp.results().isEmpty()) {
            throw new BaserowNotFoundException("Coworking meeting room booking", externalId);
        }

        if (resp.results().size() > 1) {
            throw new BaserowDataIntegrityException("Coworking meeting room booking", externalId);
        }

        return resp.results().getFirst();
    }

    @POST
    @ClientQueryParam(name = "user_field_names", value = "true")
    @Path("/{tableId}/")
    BaserowCoworkingMeetingRoomBookingRow create(
        @PathParam("tableId") int tableId,
        @NotNull @Valid CreateCoworkingMeetingRoomBookingRowRequest body
    );

    @PATCH
    @ClientQueryParam(name = "user_field_names", value = "true")
    @Path("/{tableId}/{rowId}/")
    BaserowCoworkingMeetingRoomBookingRow update(
        @PathParam("tableId") int tableId,
        @PathParam("rowId") int rowId,
        @NotNull @Valid UpdateCoworkingMeetingRoomBookingRowRequest body
    );
}
