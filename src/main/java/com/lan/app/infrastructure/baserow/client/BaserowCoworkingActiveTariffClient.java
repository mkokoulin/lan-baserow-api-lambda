package com.lan.app.infrastructure.baserow.client;

import com.lan.app.infrastructure.baserow.dto.BaserowCoworkingActiveTariffRow;
import com.lan.app.infrastructure.baserow.dto.BaserowListResponse;
import com.lan.app.infrastructure.baserow.exception.BaserowDataIntegrityException;
import com.lan.app.infrastructure.baserow.exception.BaserowNotFoundException;
import io.quarkus.rest.client.reactive.ClientQueryParam;
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
public interface BaserowCoworkingActiveTariffClient {

    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    @ClientQueryParam(name = "filter__active__equal", value = "true")
    BaserowListResponse<BaserowCoworkingActiveTariffRow> list(
        @PathParam("tableId") int tableId
    );

    @GET
    @Path("/{tableId}/{rowId}/")
    @ClientQueryParam(name = "size", value = "1")
    @ClientQueryParam(name = "user_field_names", value = "true")
    @ClientQueryParam(name = "filter__active__equal", value = "true")
    BaserowListResponse<BaserowCoworkingActiveTariffRow> getByRowId(
        @PathParam("tableId") int tableId,
        @PathParam("rowId") int rowId
    );

    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "size", value = "1")
    @ClientQueryParam(name = "user_field_names", value = "true")
    @ClientQueryParam(name = "filter__active__equal", value = "true")
    BaserowListResponse<BaserowCoworkingActiveTariffRow> findAllByExternalId(
        @PathParam("tableId") int tableId,
        @QueryParam("filter__external_id__equal") UUID externalId
    );

    default BaserowCoworkingActiveTariffRow findUniqueByExternalId(int tableId, UUID externalId) {
        var resp = findAllByExternalId(tableId, externalId);

        if (resp.count() == 0 || resp.results().isEmpty()) {
            throw new BaserowNotFoundException("Coworking active tariff", externalId);
        }

        var row = resp.results().getFirst();

        if (row.tariffId().isEmpty() || row.guestId().isEmpty()) {
            throw new BaserowDataIntegrityException("Coworking active tariff", externalId);
        }

        return row;
    }
}
