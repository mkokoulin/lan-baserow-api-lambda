package com.lan.app.infrastructure.baserow.client;

import com.lan.app.infrastructure.baserow.dto.BaserowRegistrationRow;
import com.lan.app.infrastructure.baserow.dto.CreateEventRegistrationRowRequest;
import io.quarkus.rest.client.reactive.ClientQueryParam;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "baserow")
@RegisterClientHeaders(BaserowAuthHeaders.class)
@Path("/api/database/rows/table")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface BaserowEventRegistrationClient {

    @POST
    @ClientQueryParam(name = "user_field_names", value = "true")
    @Path("/{tableId}/")
    BaserowRegistrationRow create(
        @PathParam("tableId") int tableId,
        @NotNull @Valid CreateEventRegistrationRowRequest body
    );
}
