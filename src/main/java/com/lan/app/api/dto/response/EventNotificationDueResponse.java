package com.lan.app.api.dto.response;

import java.util.List;

public record EventNotificationDueResponse(
    int id,
    String messageEn,
    String messageRu,
    String eventName,
    List<RecipientDto> recipients
) {}
