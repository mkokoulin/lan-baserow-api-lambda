package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Baserow stores event times in Armenia local time but may return them with a Z suffix
 * (e.g. "2026-06-03T15:00:00Z") where 15:00 is the local clock value, not UTC.
 * This deserializer always takes the local date-time portion, ignores any offset or Z,
 * and interprets it as Asia/Yerevan time.
 */
public class BaserowInstantDeserializer extends StdDeserializer<Instant> {

    private static final ZoneId YEREVAN = ZoneId.of("Asia/Yerevan");

    public BaserowInstantDeserializer() {
        super(Instant.class);
    }

    @Override
    public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String raw = p.getText();
        if (raw == null || raw.isBlank()) return null;
        LocalDateTime ldt;
        try {
            ldt = OffsetDateTime.parse(raw.trim()).toLocalDateTime();
        } catch (DateTimeParseException e) {
            ldt = LocalDateTime.parse(raw.trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        return ldt.atZone(YEREVAN).toInstant();
    }
}
