package com.lan.app.api.mapper;

import com.lan.app.api.dto.response.FestivalResponse;
import com.lan.app.domain.model.Festival;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApiEventsFestivalMapper {

    public FestivalResponse toResponse(Festival festival) {
        return new FestivalResponse(
            festival.id().externalId(),
            festival.name(),
            festival.description()
        );
    }
}