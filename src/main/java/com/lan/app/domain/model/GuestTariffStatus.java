package com.lan.app.domain.model;

public enum GuestTariffStatus {
    ACTIVE,
    PENDING,
    EXPIRED,
    CANCELLED,
    SUSPENDED;

    public static GuestTariffStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return switch (value.trim().toUpperCase()) {
            case "ACTIVE"    -> ACTIVE;
            case "PENDING"   -> PENDING;
            case "EXPIRED"   -> EXPIRED;
            case "CANCELLED", "CANCELED" -> CANCELLED;
            case "SUSPENDED" -> SUSPENDED;
            default -> throw new IllegalArgumentException("Unknown guest tariff status: " + value);
        };
    }
}
