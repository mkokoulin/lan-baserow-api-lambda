package com.lan.app.infrastructure.baserow.client;

import com.lan.app.infrastructure.baserow.dto.BaserowListResponse;
import com.lan.app.infrastructure.baserow.dto.BaserowReviewRow;
import com.lan.app.infrastructure.baserow.dto.CreateReviewRowRequest;
import io.quarkus.rest.client.reactive.ClientQueryParam;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "baserow")
@RegisterProvider(BaserowAuthHeaders.class)
@Path("/api/database/rows/table")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface BaserowReviewClient {

    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    @ClientQueryParam(name = "order_by", value = "-created_at")
    @ClientQueryParam(name = "filter__is_published__equal", value = "true")
    BaserowListResponse<BaserowReviewRow> list(
        @PathParam("tableId") int tableId
    );

    @POST
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    BaserowReviewRow create(
        @PathParam("tableId") int tableId,
        @NotNull @Valid CreateReviewRowRequest body
    );
}
