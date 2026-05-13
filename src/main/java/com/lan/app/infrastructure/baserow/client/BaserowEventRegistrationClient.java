package com.lan.app.infrastructure.baserow.client;

import com.lan.app.infrastructure.baserow.dto.BaserowListResponse;
import com.lan.app.infrastructure.baserow.dto.BaserowRegistrationRow;
import com.lan.app.infrastructure.baserow.dto.CreateEventRegistrationRowRequest;
import com.lan.app.infrastructure.baserow.dto.UpdateRegistrationTelegramChatIdRequest;
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
public interface BaserowEventRegistrationClient {

    @POST
    @ClientQueryParam(name = "user_field_names", value = "true")
    @Path("/{tableId}/")
    BaserowRegistrationRow create(
        @PathParam("tableId") int tableId,
        @NotNull @Valid CreateEventRegistrationRowRequest body
    );

    // filter__external_id__equal works when user_field_names=true
    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    @ClientQueryParam(name = "size", value = "1")
    BaserowListResponse<BaserowRegistrationRow> findByExternalIdRaw(
        @PathParam("tableId") int tableId,
        @QueryParam("filter__external_id__equal") UUID externalId
    );

    // filter__telegram_chat_id__equal works when user_field_names=true
    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    @ClientQueryParam(name = "size", value = "50")
    BaserowListResponse<BaserowRegistrationRow> findByChatIdRaw(
        @PathParam("tableId") int tableId,
        @QueryParam("filter__telegram_chat_id__equal") Long chatId
    );

    @PATCH
    @ClientQueryParam(name = "user_field_names", value = "true")
    @Path("/{tableId}/{rowId}/")
    BaserowRegistrationRow patchTelegramChatId(
        @PathParam("tableId") int tableId,
        @PathParam("rowId") int rowId,
        @NotNull UpdateRegistrationTelegramChatIdRequest body
    );
}
