package com.lan.app.domain.model;

public enum CoworkingTariffType {
    LONG,
    SHORT;

    public static CoworkingTariffType fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return switch (value.trim().toLowerCase()) {
            case "long" -> LONG;
            case "short" -> SHORT;
            default -> throw new IllegalArgumentException("Unknown type: " + value);
        };
    }

    public String value() {
        return this.toString().toLowerCase();
    }
}
