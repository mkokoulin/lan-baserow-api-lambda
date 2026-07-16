package com.lan.app.repository.repository;

import com.lan.app.api.dto.request.NotificationResultRequest;
import com.lan.app.domain.model.EventNotificationDue;
import com.lan.app.domain.model.EventNotificationPreview;

import java.util.List;

public interface EventNotificationRepository {
    List<EventNotificationDue> findDue();
    List<EventNotificationPreview> findDueForEvent(int eventRowId);
    void markSending(int rowId);
    void markSent(int rowId);
    void markFailed(int rowId);
    void saveResults(int notificationRowId, List<NotificationResultRequest> results);
    void recordGuestAction(int notificationRowId, int guestRowId, int registrationRowId, String action);
}
