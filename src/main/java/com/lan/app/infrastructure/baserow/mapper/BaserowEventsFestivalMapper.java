package com.lan.app.infrastructure.baserow.mapper;

import java.net.URI;

import com.lan.app.domain.model.Festival;
import com.lan.app.domain.model.Id;
import com.lan.app.infrastructure.baserow.dto.BaserowEventsFestivalRow;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaserowEventsFestivalMapper {
    
    public Festival toDomain(BaserowEventsFestivalRow festivale) {

        return new Festival(
            new Id(festivale.id(), festivale.externalId()),
            festivale.name(),
            festivale.description(),
            festivale.dateStart(),
            festivale.dateEnd(),
            festivale.isVisible(),
            festivale.isPin()
        );
    }

    private URI parseUri(String raw) {
        if (raw == null || raw.isBlank()) return null;
        return URI.create(raw);
    }
}
