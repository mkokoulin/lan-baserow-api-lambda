package com.lan.app.infrastructure.baserow.mapper;

import java.net.URI;

import com.lan.app.domain.model.Festivale;
import com.lan.app.domain.model.Id;
import com.lan.app.infrastructure.baserow.dto.BaserowFestivaleRow;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaserowFestivaleMapper {
    
    public Festivale toDomain(BaserowFestivaleRow festivale) {

        return new Festivale(
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
