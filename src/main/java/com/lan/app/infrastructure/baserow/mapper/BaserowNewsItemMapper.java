package com.lan.app.infrastructure.baserow.mapper;

import com.lan.app.domain.model.NewsItem;
import com.lan.app.infrastructure.baserow.dto.BaserowNewsItemRow;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaserowNewsItemMapper {

    public NewsItem toDomain(BaserowNewsItemRow row) {
        String imageUrl = row.image() != null && !row.image().isEmpty()
            ? row.image().getFirst().url()
            : null;
        return new NewsItem(
            row.externalId(),
            row.titleEn(),
            row.titleRu(),
            row.bodyEn(),
            row.bodyRu(),
            row.date(),
            row.category(),
            imageUrl,
            row.link()
        );
    }
}
