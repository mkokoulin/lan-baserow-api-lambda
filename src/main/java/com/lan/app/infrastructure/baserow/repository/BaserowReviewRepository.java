package com.lan.app.infrastructure.baserow.repository;

import com.lan.app.domain.model.Review;
import com.lan.app.infrastructure.baserow.client.BaserowReviewClient;
import com.lan.app.infrastructure.baserow.dto.CreateReviewRowRequest;
import com.lan.app.infrastructure.baserow.mapper.BaserowReviewMapper;
import com.lan.app.repository.ReviewRepository;
import com.lan.app.service.command.CreateReviewCommand;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@ApplicationScoped
public class BaserowReviewRepository implements ReviewRepository {

    private final int reviewsTableId;
    private final BaserowReviewClient client;
    private final BaserowReviewMapper mapper;

    BaserowReviewRepository(
        @ConfigProperty(name = "baserow.coworking.reviews-table-id") int reviewsTableId,
        @RestClient BaserowReviewClient client,
        BaserowReviewMapper mapper
    ) {
        this.reviewsTableId = reviewsTableId;
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    public List<Review> list() {
        return client.list(reviewsTableId).results().stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public Review create(CreateReviewCommand cmd) {
        var row = client.create(reviewsTableId, new CreateReviewRowRequest(
            cmd.authorName(),
            cmd.rating(),
            cmd.text()
        ));
        return mapper.toDomain(row);
    }
}
