package com.lan.app.domain.model;

public record EventCapacityAlert(
    String eventName,
    int registeredCount,
    int maxCapacity
) {}
