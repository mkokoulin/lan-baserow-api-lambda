package com.lan.app.infrastructure.baserow.mapper;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import com.lan.app.domain.model.Event;
import com.lan.app.domain.model.Id;
import com.lan.app.infrastructure.baserow.dto.BaserowEventRow;

import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
public class BaserowEventMapper {

    private static final Logger log = Logger.getLogger(BaserowEventMapper.class);
    // Armenia is UTC+4 year-round (no DST since 2012)
    private static final ZoneOffset YEREVAN = ZoneOffset.ofHours(4);

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
     * Baserow returns dates as the local Armenia clock value (e.g. "2026-06-04T11:10:00Z" where
     * 11:10 is Yerevan local time, not UTC). Strip any offset/Z and convert to UTC using +04:00.
     */
    public static Instant parseBaserowDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String s = raw.trim();
        LocalDateTime ldt;
        try {
            ldt = OffsetDateTime.parse(s).toLocalDateTime();
        } catch (DateTimeParseException e) {
            try {
                ldt = LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException e2) {
                log.warnf("Cannot parse Baserow date '%s': %s", raw, e2.getMessage());
                return null;
            }
        }
        Instant result = ldt.toInstant(YEREVAN);
        log.debugf("parseBaserowDate '%s' -> local=%s -> utc=%s", raw, ldt, result);
        return result;
    }

    private URI parseUri(String raw) {
        if (raw == null || raw.isBlank()) return null;
        return URI.create(raw);
    }
}
