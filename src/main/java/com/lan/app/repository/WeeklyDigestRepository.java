package com.lan.app.repository;

import com.lan.app.domain.model.DigestSubscriber;

import java.util.List;

public interface WeeklyDigestRepository {
    List<DigestSubscriber> findSubscribers();
    void unsubscribe(int guestRowId);
    void subscribe(int guestRowId);
}
