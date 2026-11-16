package com.lan.app.infrastructure.baserow.repository;

import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.lan.app.domain.model.NewsItem;
import com.lan.app.infrastructure.baserow.client.BaserowNewsItemClient;
import com.lan.app.infrastructure.baserow.mapper.BaserowNewsItemMapper;
import com.lan.app.repository.NewsItemRepository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaserowNewsItemRepository implements NewsItemRepository {

    private final int tableId;

    private final BaserowNewsItemClient client;
    private final BaserowNewsItemMapper mapper;

    BaserowNewsItemRepository(
        @ConfigProperty(name = "baserow.coworking.news-items-table-id") int tableId,
        @RestClient BaserowNewsItemClient client,
        BaserowNewsItemMapper mapper
    ) {
       this.tableId = tableId;
       this.client = client;
       this.mapper = mapper;
    }

    public List<NewsItem> list() {
        var row = client.list(tableId);
        return row.results().stream().map(mapper::toDomain).toList();
    }
}
