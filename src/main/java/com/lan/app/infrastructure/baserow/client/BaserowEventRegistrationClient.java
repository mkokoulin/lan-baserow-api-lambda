package com.lan.app.infrastructure.baserow.client;
import com.baserow.client.BaserowAuthHeaders;

import com.baserow.dto.BaserowListResponse;
import com.lan.app.infrastructure.baserow.dto.BaserowRegistrationRow;
import com.lan.app.infrastructure.baserow.dto.CreateEventRegistrationRowRequest;
import com.lan.app.infrastructure.baserow.dto.UpdateRegistrationIsPaidRequest;
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
public interface BaserowEventRegistrationClient {

    @POST
    @ClientQueryParam(name = "user_field_names", value = "true")
    @Path("/{tableId}/")
    BaserowRegistrationRow create(
        @PathParam("tableId") int tableId,
        @NotNull @Valid CreateEventRegistrationRowRequest body
    );

    // Finds a registration by its external UUID (used when storing chatId on confirm)
    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    @ClientQueryParam(name = "size", value = "1")
    BaserowListResponse<BaserowRegistrationRow> findByExternalIdRaw(
        @PathParam("tableId") int tableId,
        @QueryParam("filter__external_id__equal") UUID externalId
    );

    // filter__guest_id__link_row_has returns registrations linked to the given guest row ID
    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    @ClientQueryParam(name = "size", value = "50")
    BaserowListResponse<BaserowRegistrationRow> findByGuestRowIdRaw(
        @PathParam("tableId") int tableId,
        @QueryParam("filter__guest_id__link_row_has") int guestRowId
    );

    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    @ClientQueryParam(name = "size", value = "200")
    BaserowListResponse<BaserowRegistrationRow> findByEventRowIdRaw(
        @PathParam("tableId") int tableId,
        @QueryParam("filter__event_id__link_row_has") int eventRowId
    );

    @GET
    @ClientQueryParam(name = "user_field_names", value = "true")
    @Path("/{tableId}/{rowId}/")
    BaserowRegistrationRow getByRowId(
        @PathParam("tableId") int tableId,
        @PathParam("rowId") int rowId
    );

    @PATCH
    @ClientQueryParam(name = "user_field_names", value = "true")
    @Path("/{tableId}/{rowId}/")
    BaserowRegistrationRow updateIsPaid(
        @PathParam("tableId") int tableId,
        @PathParam("rowId") int rowId,
        @NotNull UpdateRegistrationIsPaidRequest body
    );
}
