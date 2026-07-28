package com.lan.app.service;

import java.util.List;
import java.util.UUID;

import com.lan.app.domain.model.CoworkingNew;
import com.lan.app.domain.model.Faq;
import com.lan.app.repository.CoworkingNewRepository;
import com.lan.app.repository.FaqRepository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CoworkingNewService {

    CoworkingNewRepository repo;
    FaqRepository faqRepo;

    public CoworkingNewService(CoworkingNewRepository repo, FaqRepository faqRepo) {
        this.repo = repo;
        this.faqRepo = faqRepo;
    }

    public List<CoworkingNew> list() {
        return repo.list();
    }

    public CoworkingNew get(UUID externalId) {
        return repo.get(externalId);
    }

    /** FAQ entries editors have tagged with this specific post (see the blog_post link field in Baserow). */
    public List<Faq> listFaq(UUID blogPostExternalId) {
        int rowId = repo.getRowIdByExternalId(blogPostExternalId);
        return faqRepo.listByBlogPostRowId(rowId);
    }
}