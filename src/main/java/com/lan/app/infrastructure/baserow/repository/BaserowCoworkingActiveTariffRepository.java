package com.lan.app.infrastructure.baserow.repository;

import com.lan.app.domain.model.CoworkingActiveTariff;
import com.lan.app.domain.model.CoworkingActiveTariffListItem;
import com.lan.app.infrastructure.baserow.client.BaserowCoworkingActiveTariffClient;
import com.lan.app.infrastructure.baserow.client.BaserowGuestClient;
import com.lan.app.infrastructure.baserow.client.BaserowCoworkingTariffClient;
import com.lan.app.infrastructure.baserow.dto.BaserowCoworkingActiveTariffRow;
import com.lan.app.infrastructure.baserow.exception.BaserowNotFoundException;
import com.lan.app.infrastructure.baserow.exception.BaserowUnavailableException;
import com.lan.app.infrastructure.baserow.mapper.BaserowCoworkingActiveTariffMapper;
import com.lan.app.repository.CoworkingActiveTariffRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class BaserowCoworkingActiveTariffRepository implements CoworkingActiveTariffRepository {

    private final int coworkingActiveTariffsTableId;
    private final int coworkingGuestsTableId;
    private final int coworkingTariffsTableId;

    private final BaserowCoworkingActiveTariffClient activeTariffClient;
    private final BaserowCoworkingTariffClient tariffClient;
    private final BaserowGuestClient guestClient;
    private final BaserowCoworkingActiveTariffMapper mapper;

    BaserowCoworkingActiveTariffRepository(
        @ConfigProperty(name = "baserow.coworking.active-tariffs-table-id") int activeTariffsTableId,
        @ConfigProperty(name = "baserow.coworking.tariffs-table-id") int coworkingTariffsTableId,
        @ConfigProperty(name = "baserow.guests.guests-table-id") int coworkingGuestsTableId,
        @RestClient BaserowCoworkingActiveTariffClient activeTariffClient,
        @RestClient BaserowCoworkingTariffClient tariffClient,
        @RestClient BaserowGuestClient guestClient,
        BaserowCoworkingActiveTariffMapper mapper
    ) {
        this.coworkingActiveTariffsTableId = activeTariffsTableId;
        this.coworkingGuestsTableId = coworkingGuestsTableId;
        this.coworkingTariffsTableId = coworkingTariffsTableId;
        this.activeTariffClient = activeTariffClient;
        this.tariffClient = tariffClient;
        this.guestClient = guestClient;
        this.mapper = mapper;
    }

    public List<CoworkingActiveTariffListItem> list() {
        var row = activeTariffClient.list(coworkingActiveTariffsTableId);
        return row.results().stream().map(mapper::toListItem).toList();
    }

    public CoworkingActiveTariff get(UUID externalId) {
        try {
            var row = activeTariffClient.findUniqueByExternalId(coworkingActiveTariffsTableId, externalId);

            return mapper.toDomain(
                row,
                resolveGuestExternalId(row),
                resolveTariffExternalId(row));
        } catch (BaserowNotFoundException e) {
            throw e;
        } catch (WebApplicationException e) {
            if (e.getResponse() != null && e.getResponse().getStatus() == 404) {
                throw new BaserowNotFoundException("Coworking active tariff", externalId);
            }
            throw new BaserowUnavailableException("Baserow request failed.", e);
        } catch (Exception e) {
            if (hasTimeoutCause(e)) {
                throw new BaserowUnavailableException("Baserow is unavailable.", e);
            }
            throw e;
        }
    }

    private UUID resolveGuestExternalId(BaserowCoworkingActiveTariffRow row) {
        var guestRowId = row.guestId().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Active tariff guest link is missing"))
            .id();
        return guestClient.getByRowId(coworkingGuestsTableId, guestRowId).externalId();
    }

    private UUID resolveTariffExternalId(BaserowCoworkingActiveTariffRow row) {
        var tariffRowId = row.tariffId().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Active tariff tariff link is missing"))
            .id();
        return tariffClient.getByRowId(coworkingTariffsTableId, tariffRowId).externalId();
    }

    private boolean hasTimeoutCause(Throwable throwable) {
        while (throwable != null) {
            if (throwable instanceof SocketTimeoutException) {
                return true;
            }
            throwable = throwable.getCause();
        }
        return false;
    }
}

