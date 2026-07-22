package com.lan.app.api.mapper;

import com.lan.app.api.dto.response.VacancyResponse;
import com.lan.app.domain.model.Vacancy;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApiVacancyMapper {

    public VacancyResponse toResponse(Vacancy vacancy) {
        return new VacancyResponse(
            vacancy.id().externalId(),
            vacancy.title(),
            vacancy.deadline(),
            vacancy.description(),
            vacancy.href()
        );
    }
}
