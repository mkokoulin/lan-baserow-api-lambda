package com.lan.app.infrastructure.baserow.client;
import com.baserow.client.BaserowAuthHeaders;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.lan.app.infrastructure.baserow.dto.BaserowEventRow;
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
public interface BaserowEventClient {

    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    @ClientQueryParam(name = "size", value = "200")
    BaserowListResponse<BaserowEventRow> listAll(
        @PathParam("tableId") int tableId,
        @QueryParam("page") int page
    );

    // Baserow caps a single response at 200 rows. The events table has grown past that, and
    // since the default order is row id ascending (insertion order), the newest events — the
    // ones added yesterday or scheduled for next month — fell past page 1 and silently
    // disappeared from the site. Walk every page via `next` instead of trusting one response.
    default BaserowListResponse<BaserowEventRow> list(int tableId) {
        var all = new ArrayList<BaserowEventRow>();
        int page = 1;
        BaserowListResponse<BaserowEventRow> resp;
        do {
            resp = listAll(tableId, page);
            all.addAll(resp.results());
            page++;
        } while (resp.next() != null);

        List<BaserowEventRow> filtered = all.stream()
            .filter(row -> row.name() != null && !row.name().isBlank())
            .toList();
        return new BaserowListResponse<>(resp.count(), null, null, filtered);
    }

    @GET
    @Path("/{tableId}/{rowId}/")
    @ClientQueryParam(name = "size", value = "1")
    @ClientQueryParam(name = "user_field_names", value = "true")
    BaserowEventRow getByRowId(
        @PathParam("tableId") int tableId,
        @PathParam("rowId") int rowId
    );

    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "size", value = "1")
    @ClientQueryParam(name = "user_field_names", value = "true")
    BaserowListResponse<BaserowEventRow> findAllByExternalId(
        @PathParam("tableId") int tableId,
        @QueryParam("filter__external_id__equal") UUID externalId
    );

    default BaserowEventRow findUniqueByExternalId(int tableId, UUID externalId) {
        var resp = findAllByExternalId(tableId, externalId);

        if (resp.count() == 0 || resp.results().isEmpty()) {
            throw new BaserowNotFoundException("Event", externalId);
        }

        if (resp.results().size() > 1) {
            throw new BaserowDataIntegrityException("Event", externalId);
        }

        return resp.results().getFirst();
    }
}
