package com.lan.app.api.mapper;

import com.lan.app.api.dto.response.FestivaleResponse;
import com.lan.app.domain.model.Festivale;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApiFestivaleMapper {

    public FestivaleResponse toResponse(Festivale festivale) {
        return new FestivaleResponse(
            festivale.id().externalId(),
            festivale.name(),
            festivale.description()
        );
    }
}