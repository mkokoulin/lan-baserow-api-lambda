package com.lan.app.infrastructure.baserow.client;

import com.lan.app.infrastructure.baserow.dto.*;
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
public interface BaserowCoworkingGuestClient {

    @GET
    @Path("/{tableId}/{rowId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    BaserowCoworkingGuestRow getByRowId(
        @PathParam("tableId") int tableId,
        @PathParam("rowId") int rowId
    );

    @GET
    @ClientQueryParam(name = "user_field_names", value = "true")
    @ClientQueryParam(name = "size", value = "1")
    @Path("/{tableId}/")
    BaserowListResponse<BaserowCoworkingGuestRow> findByExternalIdRaw(
        @PathParam("tableId") int tableId,
        @QueryParam("filter__field_7464331__equal") UUID externalId
    );

    default BaserowCoworkingGuestRow findUniqueByExternalId(int tableId, UUID externalId) {
        var resp = findByExternalIdRaw(tableId, externalId);

        if (resp.count() == 0 || resp.results().isEmpty()) {
            throw new BaserowNotFoundException("Coworking guest", externalId);
        }

        if (resp.results().size() > 1) {
            throw new BaserowDataIntegrityException("Coworking guest", externalId);
        }

        return resp.results().getFirst();
    }

    @POST
    @ClientQueryParam(name = "user_field_names", value = "true")
    @Path("/{tableId}/")
    BaserowCoworkingGuestRow create(
        @PathParam("tableId") int tableId,
        @NotNull @Valid CreateCoworkingGuestRowRequest body
    );

    @PATCH
    @ClientQueryParam(name = "user_field_names", value = "true")
    @Path("/{tableId}/{rowId}/")
    BaserowCoworkingGuestRow update(
        @PathParam("tableId") int tableId,
        @PathParam("rowId") int rowId,
        @NotNull @Valid UpdateCoworkingGuestRowRequest body
    );
}