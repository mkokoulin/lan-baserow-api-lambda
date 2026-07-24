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

    public Event toDomain(BaserowEventRow event, boolean soldOut, Integer availableSpots) {
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
            parseUri(event.telegramUrl()),
            event.showForm(),
            notifications,
            event.comment(),
            event.position(),
            event.showOnHome(),
            event.isVisible() == null || event.isVisible(),
            event.requiresPrepayment() != null && event.requiresPrepayment(),
            event.price(),
            imageUrl,
            event.maxCapacity(),
            soldOut,
            availableSpots
        );
    }

    /**
     * Baserow returns dates either:
     * - With timezone (e.g. "2026-06-05T05:45:00Z" or "...+04:00") — already correct UTC, use as-is.
     * - Without timezone (e.g. "2026-06-05T09:45:00") — treat as Yerevan local, convert to UTC.
     */
    public static Instant parseBaserowDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String s = raw.trim();
        try {
            Instant result = OffsetDateTime.parse(s).toInstant();
            log.debugf("parseBaserowDate '%s' -> utc=%s", raw, result);
            return result;
        } catch (DateTimeParseException e) {
            try {
                Instant result = LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .toInstant(YEREVAN);
                log.debugf("parseBaserowDate '%s' (no tz, treating as Yerevan) -> utc=%s", raw, result);
                return result;
            } catch (DateTimeParseException e2) {
                log.warnf("Cannot parse Baserow date '%s': %s", raw, e2.getMessage());
                return null;
            }
        }
    }

    private URI parseUri(String raw) {
        if (raw == null || raw.isBlank()) return null;
        return URI.create(raw);
    }
}
