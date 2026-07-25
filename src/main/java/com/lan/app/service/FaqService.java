package com.lan.app.service;

import java.util.List;
import java.util.UUID;

import com.lan.app.domain.model.Faq;
import com.lan.app.repository.FaqRepository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FaqService {

    FaqRepository repo;

    public FaqService(FaqRepository repo) {
        this.repo = repo;
    }

    public List<Faq> list() {
        return repo.list();
    }

    public Faq get(UUID externalId) {
        return repo.get(externalId);
    }
}
