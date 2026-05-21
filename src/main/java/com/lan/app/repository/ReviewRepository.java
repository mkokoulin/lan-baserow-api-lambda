package com.lan.app.repository;

import com.lan.app.domain.model.Review;
import com.lan.app.service.command.CreateReviewCommand;

import java.util.List;

public interface ReviewRepository {
    List<Review> list();
    Review create(CreateReviewCommand cmd);
}
