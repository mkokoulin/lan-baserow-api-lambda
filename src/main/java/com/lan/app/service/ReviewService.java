package com.lan.app.service;

import com.lan.app.domain.model.Review;
import com.lan.app.repository.ReviewRepository;
import com.lan.app.service.command.CreateReviewCommand;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class ReviewService {

    private final ReviewRepository repo;

    public ReviewService(ReviewRepository repo) {
        this.repo = repo;
    }

    public List<Review> list() {
        return repo.list();
    }

    public Review create(CreateReviewCommand cmd) {
        return repo.create(cmd);
    }
}
