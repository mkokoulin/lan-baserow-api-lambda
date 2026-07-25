package com.lan.app.api.mapper;

import com.lan.app.api.dto.response.InitiativeResponse;
import com.lan.app.domain.model.Initiative;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApiInitiativeMapper {

    public InitiativeResponse toResponse(Initiative initiative) {
        return new InitiativeResponse(
            initiative.id().externalId(),
            initiative.titleEn(),
            initiative.titleRu(),
            initiative.descriptionEn(),
            initiative.descriptionRu(),
            initiative.imageUrl(),
            initiative.href()
        );
    }
}
