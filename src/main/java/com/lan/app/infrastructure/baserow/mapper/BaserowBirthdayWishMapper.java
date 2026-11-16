package com.lan.app.infrastructure.baserow.mapper;

import com.lan.app.domain.model.BirthdayWish;
import com.lan.app.infrastructure.baserow.dto.BaserowBirthdayWishRow;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaserowBirthdayWishMapper {

    public BirthdayWish toDomain(BaserowBirthdayWishRow row) {
        return new BirthdayWish(
            row.externalId(),
            row.authorName(),
            row.telegramUserId(),
            row.text(),
            row.createdAt()
        );
    }
}
