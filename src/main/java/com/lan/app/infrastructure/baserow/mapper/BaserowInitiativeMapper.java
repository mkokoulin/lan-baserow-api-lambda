package com.lan.app.infrastructure.baserow.mapper;

import com.lan.app.domain.model.Id;
import com.lan.app.domain.model.Initiative;
import com.lan.app.infrastructure.baserow.dto.BaserowInitiativeRow;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaserowInitiativeMapper {

    public Initiative toDomain(BaserowInitiativeRow row) {
        String imageUrl = row.image() != null && !row.image().isEmpty()
            ? row.image().getFirst().url()
            : null;
        return new Initiative(
            new Id(row.id(), row.externalId()),
            row.title(),
            row.description(),
            imageUrl,
            row.href()
        );
    }
}
