package com.lan.app.api.mapper;

import com.lan.app.api.dto.response.CoworkingNewResponse;
import com.lan.app.domain.model.CoworkingNew;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApiCoworkingNewMapper {

    public CoworkingNewResponse toResponse(CoworkingNew r) {
        return new CoworkingNewResponse(
            r.id(),
            r.titleEn(),
            r.titleRu(),
            r.bodyEn(),
            r.bodyRu(),
            r.imageUrl(),
            r.link()
        );
    }
}
