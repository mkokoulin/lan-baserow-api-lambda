package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

/**
 * Baserow returns date fields without timezone info (e.g. "2026-06-03T15:00:00"),
 * representing local Armenia time. This deserializer treats such strings as Asia/Yerevan
 * and converts to UTC Instant. If the string already includes an offset or 'Z', that takes precedence.
 */
public class BaserowInstantDeserializer extends StdDeserializer<Instant> {

    private static final ZoneId YEREVAN = ZoneId.of("Asia/Yerevan");
    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
        .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        .optionalStart()
        .appendOffsetId()
        .optionalEnd()
        .toFormatter()
        .withZone(YEREVAN);

    public BaserowInstantDeserializer() {
        super(Instant.class);
    }

    @Override
    public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String raw = p.getText();
        if (raw == null || raw.isBlank()) return null;
        return FORMATTER.parse(raw.trim(), Instant::from);
    }
}
