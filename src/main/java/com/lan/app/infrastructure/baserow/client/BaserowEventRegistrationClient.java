package com.lan.app.infrastructure.baserow.client;

import com.lan.app.infrastructure.baserow.dto.BaserowListResponse;
import com.lan.app.infrastructure.baserow.dto.BaserowRegistrationRow;
import com.lan.app.infrastructure.baserow.dto.CreateEventRegistrationRowRequest;
import io.quarkus.rest.client.reactive.ClientQueryParam;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.UUID;

@RegisterRestClient(configKey = "baserow")
@RegisterClientHeaders(BaserowAuthHeaders.class)
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
}
