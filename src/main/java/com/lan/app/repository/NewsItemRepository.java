package com.lan.app.repository;

import java.util.List;

import com.lan.app.domain.model.NewsItem;

public interface NewsItemRepository {
    List<NewsItem> list();
}
