package com.lan.app.api.dto.response;

import java.util.List;

public record EventNotificationDueResponse(
    int id,
    String message,
    String eventName,
    List<RecipientDto> recipients
) {}
