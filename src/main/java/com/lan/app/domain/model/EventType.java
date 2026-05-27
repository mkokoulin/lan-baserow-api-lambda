package com.lan.app.domain.model;

public enum EventType {
    EVENT,
    FESTIVAL;

    public static EventType fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("type is empty");
        }

        return switch (value.trim().toLowerCase()) {
            case "event" -> EVENT;
            case "festival" -> FESTIVAL;
            default -> throw new IllegalArgumentException("Unknown type: " + value);
        };
    }

    public String value() {
        return this.toString().toLowerCase();
    }
}
