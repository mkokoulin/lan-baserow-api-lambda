package com.lan.app.infrastructure.baserow.client;

import com.lan.app.infrastructure.baserow.dto.BaserowEventGuestRow;
import com.lan.app.infrastructure.baserow.dto.BaserowListResponse;
import com.lan.app.infrastructure.baserow.dto.CreateEventGuestRowRequest;
import com.lan.app.infrastructure.baserow.exception.BaserowDataIntegrityException;
import com.lan.app.infrastructure.baserow.exception.BaserowNotFoundException;
import io.quarkus.rest.client.reactive.ClientQueryParam;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.UUID;

@RegisterRestClient(configKey = "baserow")
@RegisterClientHeaders(BaserowAuthHeaders.class)
@Path("/api/database/rows/table")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface BaserowEventGuestClient {

    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    @ClientQueryParam(name = "size", value = "1")
    BaserowListResponse<BaserowEventGuestRow> findByExternalIdRaw(
        @PathParam("tableId") int tableId,
        @QueryParam("filter__field_7099814__equal") UUID externalId
    );

    default BaserowEventGuestRow findUniqueByExternalId(int tableId, UUID externalId) {
        var resp = findByExternalIdRaw(tableId, externalId);

        if (resp.count() == 0 || resp.results().isEmpty()) {
            throw new BaserowNotFoundException("Event guest", externalId);
        }

        if (resp.results().size() > 1) {
            throw new BaserowDataIntegrityException("Event guest", externalId);
        }

        return resp.results().getFirst();
    }

    @POST
    @ClientQueryParam(name = "user_field_names", value = "true")
    @Path("/{tableId}/")
    BaserowEventGuestRow create(
        @PathParam("tableId") int tableId,
        @NotNull @Valid CreateEventGuestRowRequest body
    );
}
