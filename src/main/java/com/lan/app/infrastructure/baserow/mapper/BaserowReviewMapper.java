package com.lan.app.infrastructure.baserow.mapper;

import com.lan.app.domain.model.Review;
import com.lan.app.infrastructure.baserow.dto.BaserowReviewRow;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaserowReviewMapper {

    public Review toDomain(BaserowReviewRow row) {
        return new Review(
            row.externalId(),
            row.authorName(),
            row.rating(),
            row.text(),
            row.createdAt()
        );
    }
}
