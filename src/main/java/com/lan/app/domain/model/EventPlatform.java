package com.lan.app.domain.model;

public enum EventPlatform {
    LAN_SITE,
    LAN_BOT;

    public static EventPlatform fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("type is empty");
        }

        return switch (value.trim().toLowerCase()) {
            case "lan_site" -> LAN_SITE;
            case "lan_bot" -> LAN_BOT;
            default -> throw new IllegalArgumentException("Unknown type: " + value);
        };
    }

    public String value() {
        return this.toString().toLowerCase();
    }
}
