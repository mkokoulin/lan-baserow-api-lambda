package com.lan.app.service;

import com.lan.app.domain.model.DigestSubscriber;
import com.lan.app.repository.WeeklyDigestRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class WeeklyDigestService {

    private final WeeklyDigestRepository repo;

    public WeeklyDigestService(WeeklyDigestRepository repo) {
        this.repo = repo;
    }

    public List<DigestSubscriber> findSubscribers() {
        return repo.findSubscribers();
    }

    public void unsubscribe(int guestRowId) {
        repo.unsubscribe(guestRowId);
    }
}
