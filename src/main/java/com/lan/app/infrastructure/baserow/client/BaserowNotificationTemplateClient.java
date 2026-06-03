package com.lan.app.infrastructure.baserow.client;

import com.baserow.client.BaserowAuthHeaders;
import com.lan.app.infrastructure.baserow.dto.BaserowNotificationRow;
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
public interface BaserowNotificationTemplateClient {

    @GET
    @Path("/{tableId}/{rowId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    BaserowNotificationRow getByRowId(
        @PathParam("tableId") int tableId,
        @PathParam("rowId") int rowId
    );
}
