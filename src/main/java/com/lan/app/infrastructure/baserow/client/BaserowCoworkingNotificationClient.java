package com.lan.app.infrastructure.baserow.client;
import com.baserow.client.BaserowAuthHeaders;

import com.lan.app.infrastructure.baserow.dto.BaserowCoworkingNotificationRow;
import com.baserow.dto.BaserowListResponse;
import io.quarkus.rest.client.reactive.ClientQueryParam;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "baserow")
@RegisterProvider(BaserowAuthHeaders.class)
@Path("/api/database/rows/table")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface BaserowCoworkingNotificationClient {

    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    @ClientQueryParam(name = "size", value = "1")
    @ClientQueryParam(name = "filter__active__equal", value = "true")
    @ClientQueryParam(name = "filter__status__single_select_equal", value = "5541178")
    BaserowListResponse<BaserowCoworkingNotificationRow> list(
            @PathParam("tableId") int tableId
    );
}
