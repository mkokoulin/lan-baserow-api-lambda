package com.lan.app.infrastructure.baserow.mapper;

import com.lan.app.domain.model.CoworkingNew;
import com.lan.app.infrastructure.baserow.dto.BaserowCoworkingNewRow;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaserowCoworkingNewMapper {

    public CoworkingNew toDomain(BaserowCoworkingNewRow row) {
        return new CoworkingNew(
            row.externalId(),
            row.titleEn(),
            row.titleRu(),
            row.bodyEn(),
            row.bodyRu(),
            row.link()
        );
    }
}