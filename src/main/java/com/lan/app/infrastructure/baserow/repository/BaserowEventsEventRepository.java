package com.lan.app.infrastructure.baserow.repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.lan.app.domain.model.Event;
import com.lan.app.infrastructure.baserow.client.BaserowEventClient;
import com.lan.app.infrastructure.baserow.dto.BaserowEventRow;
import com.lan.app.infrastructure.baserow.mapper.BaserowEventMapper;
import com.lan.app.repository.EventRepository;
import com.lan.app.service.EventCapacityService;

import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaserowEventsEventRepository implements EventRepository {

    private final int eventTableId;

    private final BaserowEventClient eventClient;
    private final BaserowEventMapper mapper;
    private final EventCapacityService capacityService;

    BaserowEventsEventRepository(
        @ConfigProperty(name = "baserow.events.events-table-id") int eventTableId,
        @RestClient BaserowEventClient eventClient,
        BaserowEventMapper mapper,
        EventCapacityService capacityService
    ) {
        this.eventTableId = eventTableId;
        this.eventClient = eventClient;
        this.mapper = mapper;
        this.capacityService = capacityService;
    }

    // Cached (quarkus.cache.caffeine.events.* in application.properties) — this is the hot path
    // hit on every /events page load. get() below stays uncached: it backs the sold-out check
    // in EventRegistrationService.create(), which needs a fresh count, not a stale cached one.
    @CacheResult(cacheName = "events")
    public List<Event> list() {
        var row = eventClient.list(eventTableId);
        // One Baserow round trip for all registrations instead of one per event (was 2N calls
        // for N events via capacityService.isSoldOut/remainingCapacity — timed out the site's
        // 5s fetch once the events table grew).
        var guestCounts = capacityService.registeredGuestCountsByEvent();
        return row.results().stream().map(r -> toDomainWithCapacity(r, guestCounts)).toList();
    }

    public Event get(UUID externalId) {
        var row = eventClient.findUniqueByExternalId(eventTableId, externalId);
        boolean soldOut = capacityService.isSoldOut(row.maxCapacity(), row.id());
        Integer availableSpots = capacityService.remainingCapacity(row.maxCapacity(), row.id());
        return mapper.toDomain(row, soldOut, availableSpots);
    }

    @Override
    public Event getByRowId(int rowId) {
        var row = eventClient.getByRowId(eventTableId, rowId);
        boolean soldOut = capacityService.isSoldOut(row.maxCapacity(), row.id());
        Integer availableSpots = capacityService.remainingCapacity(row.maxCapacity(), row.id());
        return mapper.toDomain(row, soldOut, availableSpots);
    }

    private Event toDomainWithCapacity(BaserowEventRow row, Map<Integer, Integer> guestCounts) {
        int registeredCount = guestCounts.getOrDefault(row.id(), 0);
        boolean soldOut = row.maxCapacity() != null && row.maxCapacity() - registeredCount <= 0;
        Integer availableSpots = row.maxCapacity() == null ? null : Math.max(0, row.maxCapacity() - registeredCount);
        return mapper.toDomain(row, soldOut, availableSpots);
    }
}
