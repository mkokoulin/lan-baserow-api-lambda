package com.lan.app.infrastructure.baserow.client;

import java.util.UUID;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.lan.app.infrastructure.baserow.dto.BaserowFestivaleRow;
import com.lan.app.infrastructure.baserow.dto.BaserowListResponse;
import com.lan.app.infrastructure.baserow.exception.BaserowDataIntegrityException;
import com.lan.app.infrastructure.baserow.exception.BaserowNotFoundException;

import io.quarkus.rest.client.reactive.ClientQueryParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@RegisterRestClient(configKey = "baserow")
@RegisterClientHeaders(BaserowAuthHeaders.class)
@Path("/api/database/rows/table")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface BaserowFestivaleClient {
    
    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    BaserowListResponse<BaserowFestivaleRow> listAll(
        @PathParam("tableId") int tableId
    );

    default BaserowListResponse<BaserowFestivaleRow> list(int tableId) {
        var resp = listAll(tableId);
        var filtered = resp.results().stream()
            .filter(row -> row.name() != null && !row.name().isBlank())
            .toList();
        return new BaserowListResponse<>(resp.count(), resp.next(), resp.previous(), filtered);
    }

    @GET
    @Path("/{tableId}/{rowId}/")
    @ClientQueryParam(name = "size", value = "1")
    @ClientQueryParam(name = "user_field_names", value = "true")
    BaserowFestivaleRow getByRowId(
        @PathParam("tableId") int tableId,
        @PathParam("rowId") int rowId
    );

    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "size", value = "1")
    @ClientQueryParam(name = "user_field_names", value = "true")
    BaserowListResponse<BaserowFestivaleRow> findAllByExternalId(
        @PathParam("tableId") int tableId,
        @QueryParam("filter__field_externalId__equal") UUID externalId
    );

    default BaserowFestivaleRow findUniqueByExternalId(int tableId, UUID externalId) {
        var resp = findAllByExternalId(tableId, externalId);

        if (resp.count() == 0 || resp.results().isEmpty()) {
            throw new BaserowNotFoundException("Festivale", externalId);
        }

        if (resp.results().size() > 1) {
            throw new BaserowDataIntegrityException("Festivale", externalId);
        }

        return resp.results().getFirst();
    }
}