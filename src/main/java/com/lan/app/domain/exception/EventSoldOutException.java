package com.lan.app.domain.exception;

import java.util.Map;

/**
 * Thrown when a registration cannot be created because the event has no remaining capacity
 * for the requested guest count. Distinct from a generic {@link BusinessConflictException} so
 * clients (e.g. the Telegram bot) can tell "event is full" apart from other 409 conflicts.
 */
public class EventSoldOutException extends BusinessConflictException {

    public EventSoldOutException(String message, Map<String, Object> details) {
        super(message, details);
    }
}
