package com.lan.app.infrastructure.baserow.client;

import com.lan.app.infrastructure.baserow.dto.*;
import com.lan.app.infrastructure.baserow.exception.BaserowDataIntegrityException;
import com.lan.app.infrastructure.baserow.exception.BaserowNotFoundException;
import io.quarkus.rest.client.reactive.ClientQueryParam;
import jakarta.validation.Valid;
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
public interface BaserowGuestClient {

    @GET
    @Path("/{tableId}/{rowId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    BaserowGuestRow getByRowId(
        @PathParam("tableId") int tableId,
        @PathParam("rowId") int rowId
    );

    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    @ClientQueryParam(name = "size", value = "1")
    BaserowListResponse<BaserowGuestRow> findByExternalIdRaw(
        @PathParam("tableId") int tableId,
        @QueryParam("filter__field_7464331__equal") UUID externalId
    );

    default BaserowGuestRow findUniqueByExternalId(int tableId, UUID externalId) {
        var resp = findByExternalIdRaw(tableId, externalId);
        if (resp.count() == 0 || resp.results().isEmpty()) {
            throw new BaserowNotFoundException("Guest", externalId);
        }
        if (resp.results().size() > 1) {
            throw new BaserowDataIntegrityException("Guest", externalId);
        }
        return resp.results().getFirst();
    }

    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    @ClientQueryParam(name = "size", value = "1")
    BaserowListResponse<BaserowGuestRow> findByChatIdRaw(
        @PathParam("tableId") int tableId,
        @QueryParam("filter__telegram_chat_id__equal") Long chatId
    );

    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    @ClientQueryParam(name = "size", value = "1")
    BaserowListResponse<BaserowGuestRow> findByPhoneRaw(
        @PathParam("tableId") int tableId,
        @QueryParam("filter__phone__equal") String phone
    );

    @POST
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    BaserowGuestRow create(
        @PathParam("tableId") int tableId,
        @NotNull @Valid CreateGuestRowRequest body
    );

    @PATCH
    @Path("/{tableId}/{rowId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    BaserowGuestRow update(
        @PathParam("tableId") int tableId,
        @PathParam("rowId") int rowId,
        @NotNull @Valid UpdateCoworkingGuestRowRequest body
    );

    @PATCH
    @Path("/{tableId}/{rowId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    BaserowGuestRow patchChatId(
        @PathParam("tableId") int tableId,
        @PathParam("rowId") int rowId,
        @NotNull LinkChatIdRowRequest body
    );
}
