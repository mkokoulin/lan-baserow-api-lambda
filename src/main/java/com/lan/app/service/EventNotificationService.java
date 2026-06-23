package com.lan.app.service;

import com.lan.app.api.dto.request.NotificationResultRequest;
import com.lan.app.domain.model.EventNotificationDue;
import com.lan.app.repository.repository.EventNotificationRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class EventNotificationService {

    private final EventNotificationRepository repo;

    public EventNotificationService(EventNotificationRepository repo) {
        this.repo = repo;
    }

    public List<EventNotificationDue> findDue() {
        return repo.findDue();
    }

    public void markSent(int rowId) {
        repo.markSent(rowId);
    }

    public void markFailed(int rowId) {
        repo.markFailed(rowId);
    }

    public void saveResults(int notificationRowId, List<NotificationResultRequest> results) {
        repo.saveResults(notificationRowId, results);
    }
}
