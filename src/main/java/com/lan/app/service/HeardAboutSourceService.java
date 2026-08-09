package com.lan.app.service;

import com.lan.app.domain.model.HeardAboutSourceRecipient;
import com.lan.app.repository.HeardAboutSourceRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class HeardAboutSourceService {

    private final HeardAboutSourceRepository repo;

    public HeardAboutSourceService(HeardAboutSourceRepository repo) {
        this.repo = repo;
    }

    public List<HeardAboutSourceRecipient> findDue() {
        return repo.findDue();
    }

    public void saveAnswer(int guestRowId, String source, String comment) {
        repo.saveAnswer(guestRowId, source, comment);
    }
}
