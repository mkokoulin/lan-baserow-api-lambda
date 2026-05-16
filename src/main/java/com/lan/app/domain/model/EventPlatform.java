package com.lan.app.domain.model;

import com.lan.app.infrastructure.baserow.dto.BaserowSelectOption;

public enum EventPlatform {
    LAN_SITE,
    LAN_BOT;

    public static EventPlatform fromBaserow(BaserowSelectOption raw) {
        if (raw == null || raw.value() == null || raw.value().isBlank()) {
            throw new IllegalArgumentException("type is empty");
        }

        return switch (raw.value().trim().toLowerCase()) {
            case "lan_site" -> LAN_SITE;
            case "lan_bot" -> LAN_BOT;
            default -> throw new IllegalArgumentException("Unknown type: " + raw);
        };
    }

    public String value() {
        return this.toString().toLowerCase();
    }
}
