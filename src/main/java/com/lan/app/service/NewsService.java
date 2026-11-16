package com.lan.app.service;

import java.util.List;

import com.lan.app.domain.model.NewsItem;
import com.lan.app.repository.NewsItemRepository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NewsService {

    NewsItemRepository repo;

    public NewsService(NewsItemRepository repo) {
        this.repo = repo;
    }

    public List<NewsItem> list() {
        return repo.list();
    }
}
