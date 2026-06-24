package com.lan.app.api.mapper;

import com.lan.app.api.dto.response.EventNotificationResponse;
import com.lan.app.domain.model.EventNotification;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EventNotificationMapper {

    public EventNotificationResponse toResponse(EventNotification notification) {
        return new EventNotificationResponse(
            notification.id().externalId().toString(),
            notification.leadHours(),
            notification.message(),
            notification.active()
        );
    }
}
