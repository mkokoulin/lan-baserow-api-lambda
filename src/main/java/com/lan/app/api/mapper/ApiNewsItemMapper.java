package com.lan.app.api.mapper;

import com.lan.app.api.dto.response.NewsItemResponse;
import com.lan.app.domain.model.NewsItem;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApiNewsItemMapper {

    public NewsItemResponse toResponse(NewsItem n) {
        return new NewsItemResponse(
            n.id(),
            n.titleEn(),
            n.titleRu(),
            n.bodyEn(),
            n.bodyRu(),
            n.date(),
            n.category(),
            n.imageUrl(),
            n.link()
        );
    }
}
