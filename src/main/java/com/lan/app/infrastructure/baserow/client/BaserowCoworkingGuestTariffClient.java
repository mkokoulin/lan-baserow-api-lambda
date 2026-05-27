package com.lan.app.infrastructure.baserow.client;

import java.util.UUID;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.lan.app.infrastructure.baserow.dto.BaserowCoworkingActiveTariffRow;
import com.lan.app.infrastructure.baserow.dto.BaserowCoworkingGuestTariffRow;
import com.lan.app.infrastructure.baserow.dto.BaserowListResponse;
import com.lan.app.infrastructure.baserow.dto.CreateGuestTariffRowRequest;
import com.lan.app.infrastructure.baserow.dto.UpdateGuestTariffDaysUsedRequest;
import com.lan.app.infrastructure.baserow.exception.BaserowDataIntegrityException;
import com.lan.app.infrastructure.baserow.exception.BaserowNotFoundException;

import io.quarkus.rest.client.reactive.ClientQueryParam;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;

@RegisterRestClient(configKey = "baserow")
@RegisterProvider(BaserowAuthHeaders.class)
@RegisterProvider(BaserowErrorLoggingFilter.class)
@Path("/api/database/rows/table")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface BaserowCoworkingGuestTariffClient {
    
    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    @ClientQueryParam(name = "filter__active__equal", value = "true")
    BaserowListResponse<BaserowCoworkingGuestTariffRow> list(
        @PathParam("tableId") int tableId
    );

    @GET
    @Path("/{tableId}/{rowId}/")
    @ClientQueryParam(name = "size", value = "1")
    @ClientQueryParam(name = "user_field_names", value = "true")
    @ClientQueryParam(name = "filter__active__equal", value = "true")
    BaserowListResponse<BaserowCoworkingGuestTariffRow> getByRowId(
        @PathParam("tableId") int tableId,
        @PathParam("rowId") int rowId
    );

    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "size", value = "1")
    @ClientQueryParam(name = "user_field_names", value = "true")
    @ClientQueryParam(name = "filter__active__equal", value = "true")
    BaserowListResponse<BaserowCoworkingGuestTariffRow> findAllByExternalId(
        @PathParam("tableId") int tableId,
        @QueryParam("filter__external_id__equal") UUID externalId
    );

    @GET
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    BaserowListResponse<BaserowCoworkingGuestTariffRow> findAllByGuestRowId(
        @PathParam("tableId") int tableId,
        @QueryParam("filter__guest_id__link_row_has") int guestRowId
    );

    @POST
    @Path("/{tableId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    BaserowCoworkingGuestTariffRow create(
        @PathParam("tableId") int tableId,
        @NotNull @Valid CreateGuestTariffRowRequest body
    );

    @PATCH
    @Path("/{tableId}/{rowId}/")
    @ClientQueryParam(name = "user_field_names", value = "true")
    BaserowCoworkingGuestTariffRow patchDaysUsed(
        @PathParam("tableId") int tableId,
        @PathParam("rowId") int rowId,
        @NotNull UpdateGuestTariffDaysUsedRequest body
    );

    default BaserowCoworkingGuestTariffRow findUniqueByExternalId(int tableId, UUID externalId) {
        var resp = findAllByExternalId(tableId, externalId);

        if (resp.count() == 0 || resp.results().isEmpty()) {
            throw new BaserowNotFoundException("Coworking guest tariff", externalId);
        }

        var row = resp.results().getFirst();

        if (row.tariffId().isEmpty() || row.guestId().isEmpty()) {
            throw new BaserowDataIntegrityException("Coworking guest tariff", externalId);
        }

        return row;
    }
}
