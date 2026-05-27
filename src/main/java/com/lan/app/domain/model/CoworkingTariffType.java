package com.lan.app.domain.model;

import com.lan.app.infrastructure.baserow.dto.BaserowSingleSelect;

public enum CoworkingTariffType {
    LONG,
    SHORT;

    public static CoworkingTariffType fromBaserow(BaserowSingleSelect raw) {
        if (raw == null || raw.value() == null || raw.value().isBlank()) {
            return null;
        }

        return switch (raw.value().trim().toLowerCase()) {
            case "long" -> LONG;
            case "short" -> SHORT;
            default -> throw new IllegalArgumentException("Unknown type: " + raw);
        };
    }

    public String value() {
        return this.toString().toLowerCase();
    }
}
