package com.lan.app.infrastructure.baserow.repository;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.lan.app.domain.model.Faq;
import com.lan.app.infrastructure.baserow.client.BaserowFaqClient;
import com.lan.app.infrastructure.baserow.mapper.BaserowFaqMapper;
import com.lan.app.repository.FaqRepository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaserowFaqRepository implements FaqRepository {

    private final int faqTableId;

    private final BaserowFaqClient client;
    private final BaserowFaqMapper mapper;

    BaserowFaqRepository(
        @ConfigProperty(name = "baserow.faq.faq-table-id") int faqTableId,
        @RestClient BaserowFaqClient client,
        BaserowFaqMapper mapper
    ) {
        this.faqTableId = faqTableId;
        this.client = client;
        this.mapper = mapper;
    }

    public List<Faq> list() {
        var row = client.list(faqTableId);
        return row.results().stream().map(mapper::toDomain).toList();
    }

    public Faq get(UUID externalId) {
        var row = client.findUniqueByExternalId(faqTableId, externalId);
        return mapper.toDomain(row);
    }

    @Override
    public List<Faq> listByBlogPostRowId(int blogPostRowId) {
        var row = client.listByBlogPostRowId(faqTableId, blogPostRowId);
        return row.results().stream().map(mapper::toDomain).toList();
    }
}
