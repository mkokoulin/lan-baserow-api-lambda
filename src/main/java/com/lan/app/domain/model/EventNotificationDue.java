package com.lan.app.domain.model;

import java.util.List;

public record EventNotificationDue(
    int rowId,
    String messageEn,
    String messageRu,
    String eventName,
    List<NotificationRecipient> recipients
) {}
