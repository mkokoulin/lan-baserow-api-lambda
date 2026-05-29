package com.lan.app.infrastructure.baserow.client;
import com.baserow.client.BaserowAuthHeaders;

import com.baserow.dto.BaserowListResponse;
import com.lan.app.infrastructure.baserow.dto.BaserowPaymentRow;
import com.lan.app.infrastructure.baserow.dto.CreatePaymentRowRequest;
import com.lan.app.infrastructure.baserow.dto.UpdatePaymentRow;
import io.quarkus.rest.client.reactive.ClientQueryParam;
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
public interface BaserowPaymentClient {

    @POST
    @ClientQueryParam(name = "user_field_names", value = "true")
    @Path("/{tableId}/")
    BaserowPaymentRow create(
        @PathParam("tableId") int tableId,
        @NotNull CreatePaymentRowRequest body
    );

    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    @ClientQueryParam(name = "size", value = "1")
    BaserowListResponse<BaserowPaymentRow> findByExternalId(
        @PathParam("tableId") int tableId,
        @QueryParam("filter__external_id__equal") UUID externalId
    );

    @PATCH
    @ClientQueryParam(name = "user_field_names", value = "true")
    @Path("/{tableId}/{rowId}/")
    BaserowPaymentRow updateStatus(
        @PathParam("tableId") int tableId,
        @PathParam("rowId") int rowId,
        @NotNull UpdatePaymentRow body
    );
}
