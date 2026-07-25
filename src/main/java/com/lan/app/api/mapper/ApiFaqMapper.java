package com.lan.app.api.mapper;

import com.lan.app.api.dto.response.FaqResponse;
import com.lan.app.domain.model.Faq;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApiFaqMapper {

    public FaqResponse toResponse(Faq faq) {
        return new FaqResponse(
            faq.id().externalId(),
            faq.questionEn(),
            faq.questionRu(),
            faq.answerEn(),
            faq.answerRu(),
            faq.position()
        );
    }
}
