package com.lan.app.infrastructure.baserow.client;
import com.baserow.client.BaserowAuthHeaders;

import java.util.UUID;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.lan.app.infrastructure.baserow.dto.BaserowEventLikeRow;
import com.lan.app.infrastructure.baserow.dto.CreateEventLikeRowRequest;
import com.baserow.dto.BaserowListResponse;

import io.quarkus.rest.client.reactive.ClientQueryParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
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
public interface BaserowEventLikeClient {

    // Same page-size ceiling as BaserowEventClient.listAll — fine at this system's scale.
    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    @ClientQueryParam(name = "size", value = "200")
    BaserowListResponse<BaserowEventLikeRow> listAll(
        @PathParam("tableId") int tableId
    );

    // size=1 — we only read the response's total `count`, not the row itself.
    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    @ClientQueryParam(name = "size", value = "1")
    BaserowListResponse<BaserowEventLikeRow> findByEvent(
        @PathParam("tableId") int tableId,
        @QueryParam("filter__event_external_id__equal") UUID eventExternalId
    );

    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    @ClientQueryParam(name = "size", value = "1")
    BaserowListResponse<BaserowEventLikeRow> findByEventAndAnon(
        @PathParam("tableId") int tableId,
        @QueryParam("filter__event_external_id__equal") UUID eventExternalId,
        @QueryParam("filter__anon_id__equal") String anonId
    );

    @POST
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    BaserowEventLikeRow create(
        @PathParam("tableId") int tableId,
        CreateEventLikeRowRequest body
    );

    @DELETE
    @Path("/{tableId}/{rowId}/")
    void delete(
        @PathParam("tableId") int tableId,
        @PathParam("rowId") int rowId
    );
}
