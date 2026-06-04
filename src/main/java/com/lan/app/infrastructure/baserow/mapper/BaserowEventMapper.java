package com.lan.app.infrastructure.baserow.mapper;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import com.lan.app.domain.model.Event;
import com.lan.app.domain.model.Id;
import com.lan.app.infrastructure.baserow.dto.BaserowEventRow;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaserowEventMapper {

    static final ZoneId YEREVAN = ZoneId.of("Asia/Yerevan");

    public Event toDomain(BaserowEventRow event) {
        var notifications = event.notifications() != null
            ? event.notifications().stream().map(n -> String.valueOf(n.id())).toList()
            : List.<String>of();

        String imageUrl = event.image() != null && !event.image().isEmpty()
            ? event.image().getFirst().url()
            : null;

        return new Event(
            new Id(event.id(), event.externalId()),
            event.name(),
            parseBaserowDate(event.dateStart()),
            parseBaserowDate(event.dateEnd()),
            event.description(),
            parseUri(event.externalRegistrationUrl()),
            event.registrationUrl(),
            parseUri(event.instagramUrl()),
            event.showForm(),
            notifications,
            event.comment(),
            event.isPin(),
            event.requiresPrepayment() != null && event.requiresPrepayment(),
            event.price(),
            imageUrl
        );
    }

    /**
     * Baserow returns dates in local Armenia time (e.g. "2026-06-04T11:10:00Z" where 11:10 is the
     * Yerevan clock value, not UTC). We strip any offset/Z suffix and interpret as Asia/Yerevan.
     */
    public static Instant parseBaserowDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        LocalDateTime ldt;
        try {
            ldt = OffsetDateTime.parse(raw.trim()).toLocalDateTime();
        } catch (DateTimeParseException e) {
            ldt = LocalDateTime.parse(raw.trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        return ldt.atZone(YEREVAN).toInstant();
    }

    private URI parseUri(String raw) {
        if (raw == null || raw.isBlank()) return null;
        return URI.create(raw);
    }
}
