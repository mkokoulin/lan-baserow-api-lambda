package com.lan.app.infrastructure.baserow.repository;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.lan.app.domain.model.Event;
import com.lan.app.infrastructure.baserow.client.BaserowEventClient;
import com.lan.app.infrastructure.baserow.dto.BaserowEventRow;
import com.lan.app.infrastructure.baserow.mapper.BaserowEventMapper;
import com.lan.app.repository.EventRepository;
import com.lan.app.service.EventCapacityService;

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

    public List<Event> list() {
        var row = eventClient.list(eventTableId);
        return row.results().stream().map(this::toDomainWithCapacity).toList();
    }

    public Event get(UUID externalId) {
        var row = eventClient.findUniqueByExternalId(eventTableId, externalId);
        return toDomainWithCapacity(row);
    }

    private Event toDomainWithCapacity(BaserowEventRow row) {
        boolean soldOut = capacityService.isSoldOut(row.maxCapacity(), row.id());
        return mapper.toDomain(row, soldOut);
    }
}
