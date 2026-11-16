package com.lan.app.api.mapper;

import com.lan.app.api.dto.response.BirthdayWishResponse;
import com.lan.app.domain.model.BirthdayWish;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApiBirthdayWishMapper {

    public BirthdayWishResponse toResponse(BirthdayWish w) {
        return new BirthdayWishResponse(
            w.id(),
            w.authorName(),
            w.telegramUserId(),
            w.text(),
            w.createdAt()
        );
    }
}
