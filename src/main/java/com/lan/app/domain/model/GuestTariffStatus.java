package com.lan.app.domain.model;

import com.lan.app.infrastructure.baserow.dto.BaserowSingleSelect;

public enum GuestTariffStatus {
    ACTIVE,
    PENDING,
    EXPIRED,
    CANCELLED,
    SUSPENDED;

    public static GuestTariffStatus fromBaserow(BaserowSingleSelect raw) {
        if (raw == null || raw.value() == null || raw.value().isBlank()) {
            return null;
        }

        return switch (raw.value().trim().toUpperCase()) {
            case "ACTIVE"    -> ACTIVE;
            case "PENDING"   -> PENDING;
            case "EXPIRED"   -> EXPIRED;
            case "CANCELLED", "CANCELED" -> CANCELLED;
            case "SUSPENDED" -> SUSPENDED;
            default -> throw new IllegalArgumentException("Unknown guest tariff status: " + raw.value());
        };
    }
}
