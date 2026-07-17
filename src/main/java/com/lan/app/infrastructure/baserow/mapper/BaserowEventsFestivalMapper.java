package com.lan.app.infrastructure.baserow.mapper;

import java.net.URI;
import java.util.UUID;

import com.lan.app.domain.model.Festival;
import com.lan.app.domain.model.Id;
import com.lan.app.infrastructure.baserow.dto.BaserowEventsFestivalRow;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaserowEventsFestivalMapper {
    
    public Festival toDomain(BaserowEventsFestivalRow festivale) {
        var eventsIds
            = festivale.eventsIds()
                .stream()
                .map(ei -> UUID.fromString(ei.value()))
                .toList();

        String imageUrl = festivale.image() != null && !festivale.image().isEmpty()
            ? festivale.image().getFirst().url()
            : null;

        return new Festival(
            new Id(festivale.id(), festivale.externalId()),
            festivale.name(),
            festivale.description(),
            eventsIds,
            BaserowEventMapper.parseBaserowDate(festivale.dateStart()),
            BaserowEventMapper.parseBaserowDate(festivale.dateEnd()),
            festivale.isVisible(),
            festivale.position(),
            festivale.showOnHome(),
            imageUrl
        );
    }

    private URI parseUri(String raw) {
        if (raw == null || raw.isBlank()) return null;
        return URI.create(raw);
    }
}
