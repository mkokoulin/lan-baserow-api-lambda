package com.lan.app.infrastructure.baserow.mapper;

import com.lan.app.domain.model.Id;
import com.lan.app.domain.model.Vacancy;
import com.lan.app.infrastructure.baserow.dto.BaserowVacancyRow;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaserowVacancyMapper {

    public Vacancy toDomain(BaserowVacancyRow vacancy) {
        return new Vacancy(
            new Id(vacancy.id(), vacancy.externalId()),
            vacancy.title(),
            vacancy.deadline(),
            vacancy.description(),
            vacancy.href()
        );
    }
}
