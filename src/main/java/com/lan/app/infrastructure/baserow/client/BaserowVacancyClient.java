package com.lan.app.infrastructure.baserow.client;

import com.baserow.client.BaserowAuthHeaders;

import java.util.UUID;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.lan.app.infrastructure.baserow.dto.BaserowVacancyRow;
import com.baserow.dto.BaserowListResponse;
import com.baserow.exception.BaserowDataIntegrityException;
import com.baserow.exception.BaserowNotFoundException;

import io.quarkus.rest.client.reactive.ClientQueryParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@RegisterRestClient(configKey = "baserow")
@RegisterProvider(BaserowAuthHeaders.class)
@Path("/api/database/rows/table")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface BaserowVacancyClient {

    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    BaserowListResponse<BaserowVacancyRow> list(
        @PathParam("tableId") int tableId
    );

    @GET
    @Path("/{tableId}/{rowId}/")
    @ClientQueryParam(name = "size", value = "1")
    @ClientQueryParam(name = "user_field_names", value = "true")
    BaserowVacancyRow getByRowId(
        @PathParam("tableId") int tableId,
        @PathParam("rowId") int rowId
    );

    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "size", value = "1")
    @ClientQueryParam(name = "user_field_names", value = "true")
    BaserowListResponse<BaserowVacancyRow> findAllByExternalId(
        @PathParam("tableId") int tableId,
        @QueryParam("filter__external_id__equal") UUID externalId
    );

    default BaserowVacancyRow findUniqueByExternalId(int tableId, UUID externalId) {
        var resp = findAllByExternalId(tableId, externalId);

        if (resp.count() == 0 || resp.results().isEmpty()) {
            throw new BaserowNotFoundException("Vacancy", externalId);
        }

        if (resp.results().size() > 1) {
            throw new BaserowDataIntegrityException("Vacancy", externalId);
        }

        return resp.results().getFirst();
    }
}
