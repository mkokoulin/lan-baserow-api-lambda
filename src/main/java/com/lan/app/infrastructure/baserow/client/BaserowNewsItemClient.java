package com.lan.app.infrastructure.baserow.client;
import com.baserow.client.BaserowAuthHeaders;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.lan.app.infrastructure.baserow.dto.BaserowNewsItemRow;
import com.baserow.dto.BaserowListResponse;

import io.quarkus.rest.client.reactive.ClientQueryParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@RegisterRestClient(configKey = "baserow")
@RegisterProvider(BaserowAuthHeaders.class)
@Path("/api/database/rows/table")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface BaserowNewsItemClient {

    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    @ClientQueryParam(name = "filter__is_visible__boolean", value = "true")
    BaserowListResponse<BaserowNewsItemRow> list(
        @PathParam("tableId") int tableId
    );
}
