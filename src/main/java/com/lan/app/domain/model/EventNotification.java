package com.lan.app.domain.model;

public record EventNotification(
    Id id,
    Integer leadHours,
    String message,
    Boolean active
) {}
