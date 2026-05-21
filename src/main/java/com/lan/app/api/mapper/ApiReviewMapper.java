package com.lan.app.api.mapper;

import com.lan.app.api.dto.response.ReviewResponse;
import com.lan.app.domain.model.Review;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApiReviewMapper {

    public ReviewResponse toResponse(Review r) {
        return new ReviewResponse(
            r.id(),
            r.authorName(),
            r.rating(),
            r.text(),
            r.createdAt()
        );
    }
}
