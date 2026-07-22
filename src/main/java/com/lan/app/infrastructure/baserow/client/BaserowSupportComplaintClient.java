package com.lan.app.infrastructure.baserow.client;

import com.baserow.client.BaserowAuthHeaders;
import com.lan.app.infrastructure.baserow.dto.BaserowSupportComplaintRow;
import com.lan.app.infrastructure.baserow.dto.CreateSupportComplaintRowRequest;
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
public interface BaserowSupportComplaintClient {

    @POST
    @ClientQueryParam(name = "user_field_names", value = "true")
    @Path("/{tableId}/")
    BaserowSupportComplaintRow create(
        @PathParam("tableId") int tableId,
        @NotNull @Valid CreateSupportComplaintRowRequest body
    );
}
