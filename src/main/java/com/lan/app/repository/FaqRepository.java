package com.lan.app.repository;

import java.util.List;
import java.util.UUID;

import com.lan.app.domain.model.Faq;

public interface FaqRepository {
    List<Faq> list();
    Faq get(UUID externalId);
    List<Faq> listByBlogPostRowId(int blogPostRowId);
}
