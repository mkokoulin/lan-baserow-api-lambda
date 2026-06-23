package com.lan.app.domain.model;

import java.util.List;

public record EventNotificationDue(
    int rowId,
    String message,
    String eventName,
    List<NotificationRecipient> recipients
) {}
