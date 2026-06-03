package com.lan.app.repository.repository;

import com.lan.app.domain.model.EventNotificationDue;

import java.util.List;

public interface EventNotificationRepository {
    List<EventNotificationDue> findDue();
    void markSending(int rowId);
    void markSent(int rowId);
    void markFailed(int rowId);
}
