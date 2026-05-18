package com.lan.app.infrastructure.baserow.client;

import com.lan.app.infrastructure.baserow.dto.BaserowCoworkingTariffRow;
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
public interface BaserowCoworkingTariffClient {

    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    BaserowListResponse<BaserowCoworkingTariffRow> list(
        @PathParam("tableId") int tableId
    );

    @GET
    @Path("/{tableId}/{rowId}/")
    @ClientQueryParam(name = "size", value = "1")
    @ClientQueryParam(name = "user_field_names", value = "true")
    BaserowCoworkingTariffRow getByRowId(
        @PathParam("tableId") int tableId,
        @PathParam("rowId") int rowId
    );

    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "size", value = "1")
    @ClientQueryParam(name = "user_field_names", value = "true")
    BaserowListResponse<BaserowCoworkingTariffRow> findAllByExternalId(
        @PathParam("tableId") int tableId,
        @QueryParam("filter__field_externalId__equal") UUID externalId
    );

    default BaserowCoworkingTariffRow findUniqueByExternalId(int tableId, UUID externalId) {
        var resp = findAllByExternalId(tableId, externalId);

        if (resp.count() == 0 || resp.results().isEmpty()) {
            throw new BaserowNotFoundException("Coworking tariff", externalId);
        }

        if (resp.results().size() > 1) {
            throw new BaserowDataIntegrityException("Coworking tariff", externalId);
        }

        return resp.results().getFirst();
    }
}
