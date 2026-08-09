package com.lan.app.repository;

import com.lan.app.domain.model.HeardAboutSourceRecipient;

import java.util.List;

public interface HeardAboutSourceRepository {
    List<HeardAboutSourceRecipient> findDue();
    void saveAnswer(int guestRowId, String source, String comment);
}
