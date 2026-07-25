package com.lan.app.infrastructure.baserow.mapper;

import com.lan.app.domain.model.Faq;
import com.lan.app.domain.model.Id;
import com.lan.app.infrastructure.baserow.dto.BaserowFaqRow;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaserowFaqMapper {

    public Faq toDomain(BaserowFaqRow row) {
        return new Faq(
            new Id(row.id(), row.externalId()),
            row.questionEn(),
            row.questionRu(),
            row.answerEn(),
            row.answerRu(),
            row.position()
        );
    }
}
