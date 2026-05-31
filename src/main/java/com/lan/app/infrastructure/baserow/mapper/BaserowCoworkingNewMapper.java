package com.lan.app.infrastructure.baserow.mapper;

import com.lan.app.domain.model.CoworkingNew;
import com.lan.app.infrastructure.baserow.dto.BaserowCoworkingNewRow;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaserowCoworkingNewMapper {

    public CoworkingNew toDomain(BaserowCoworkingNewRow row) {
        String imageUrl = row.image() != null && !row.image().isEmpty()
            ? row.image().getFirst().url()
            : null;
        return new CoworkingNew(
            row.externalId(),
            row.titleEn(),
            row.titleRu(),
            row.bodyEn(),
            row.bodyRu(),
            imageUrl,
            row.link()
        );
    }
}