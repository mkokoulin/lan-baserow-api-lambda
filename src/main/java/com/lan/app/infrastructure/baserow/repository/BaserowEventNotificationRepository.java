package com.lan.app.infrastructure.baserow.repository;

import com.baserow.repository.AbstractBaserowRepository;
import com.lan.app.api.dto.request.NotificationResultRequest;
import com.lan.app.domain.model.EventNotificationDue;
import com.lan.app.domain.model.NotificationRecipient;
import com.lan.app.infrastructure.baserow.client.BaserowEventClient;
import com.lan.app.infrastructure.baserow.client.BaserowEventNotificationClient;
import com.lan.app.infrastructure.baserow.client.BaserowEventNotificationResultClient;
import com.lan.app.infrastructure.baserow.client.BaserowEventRegistrationClient;
import com.lan.app.infrastructure.baserow.client.BaserowGuestClient;
import com.lan.app.infrastructure.baserow.client.BaserowNotificationTemplateClient;
import com.lan.app.infrastructure.baserow.dto.BaserowEventNotificationResultRow;
import com.lan.app.infrastructure.baserow.dto.BaserowEventNotificationRow;
import com.lan.app.infrastructure.baserow.dto.BaserowEventRow;
import com.lan.app.infrastructure.baserow.dto.CreateEventNotificationResultRowRequest;
import com.lan.app.infrastructure.baserow.dto.UpdateEventNotificationStatusRequest;
import com.lan.app.infrastructure.baserow.dto.UpdateNotificationResultActionRequest;
import com.lan.app.repository.repository.EventNotificationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class BaserowEventNotificationRepository extends AbstractBaserowRepository
        implements EventNotificationRepository {

    private static final Logger log = Logger.getLogger(BaserowEventNotificationRepository.class);
    private static final ZoneId YEREVAN = ZoneId.of("Asia/Yerevan");
    private static final long MAX_OVERDUE_HOURS = 24;

    private final int eventNotificationsTableId;
    private final int notificationsTableId;
    private final int eventsTableId;
    private final int registrationsTableId;
    private final int guestsTableId;
    private final int workingHourStart;
    private final int workingHourEnd;

    private final int notificationResultsTableId;

    private final BaserowEventNotificationClient client;
    private final BaserowNotificationTemplateClient notificationTemplateClient;
    private final BaserowEventClient eventClient;
    private final BaserowEventRegistrationClient registrationClient;
    private final BaserowGuestClient guestClient;
    private final BaserowEventNotificationResultClient resultClient;

    public BaserowEventNotificationRepository(
        @ConfigProperty(name = "baserow.events.event-notifications-table-id") int eventNotificationsTableId,
        @ConfigProperty(name = "baserow.events.notifications-table-id") int notificationsTableId,
        @ConfigProperty(name = "baserow.events.events-table-id") int eventsTableId,
        @ConfigProperty(name = "baserow.events.registrations-table-id") int registrationsTableId,
        @ConfigProperty(name = "baserow.guests.guests-table-id") int guestsTableId,
        @ConfigProperty(name = "baserow.events.notification-results-table-id") int notificationResultsTableId,
        @ConfigProperty(name = "app.notifications.working-hour-start", defaultValue = "9") int workingHourStart,
        @ConfigProperty(name = "app.notifications.working-hour-end", defaultValue = "21") int workingHourEnd,
        @RestClient BaserowEventNotificationClient client,
        @RestClient BaserowNotificationTemplateClient notificationTemplateClient,
        @RestClient BaserowEventClient eventClient,
        @RestClient BaserowEventRegistrationClient registrationClient,
        @RestClient BaserowGuestClient guestClient,
        @RestClient BaserowEventNotificationResultClient resultClient
    ) {
        this.eventNotificationsTableId = eventNotificationsTableId;
        this.notificationsTableId = notificationsTableId;
        this.eventsTableId = eventsTableId;
        this.registrationsTableId = registrationsTableId;
        this.guestsTableId = guestsTableId;
        this.notificationResultsTableId = notificationResultsTableId;
        this.workingHourStart = workingHourStart;
        this.workingHourEnd = workingHourEnd;
        this.client = client;
        this.notificationTemplateClient = notificationTemplateClient;
        this.eventClient = eventClient;
        this.registrationClient = registrationClient;
        this.guestClient = guestClient;
        this.resultClient = resultClient;
    }

    @Override
    public List<EventNotificationDue> findDue() {
        var rows = execute(() -> client.listActive(eventNotificationsTableId)).results();
        var now = Instant.now();
        var nowYerevan = ZonedDateTime.now(YEREVAN);

        log.infof("findDue: fetched %d active rows, now=%s (Yerevan hour=%d), working hours=%d-%d",
            rows.size(), now, nowYerevan.getHour(), workingHourStart, workingHourEnd);

        if (!isWorkingHour(nowYerevan)) {
            log.infof("Outside working hours (%d), skipping event notifications", nowYerevan.getHour());
            return List.of();
        }

        var result = new ArrayList<EventNotificationDue>();

        for (var row : rows) {
            String rowStatus = row.status() != null ? row.status().value() : "null";
            if (!isPending(row)) {
                log.infof("Row %d skipped: status=%s (not pending)", row.id(), rowStatus);
                continue;
            }
            if (row.eventId() == null || row.eventId().isEmpty()) {
                log.infof("Row %d skipped: no event_id linked", row.id());
                continue;
            }
            if (row.notifications() == null || row.notifications().isEmpty()) {
                log.infof("Row %d skipped: no notification templates linked", row.id());
                continue;
            }

            int eventRowId = row.eventId().getFirst().id();
            log.infof("Row %d: processing, eventRowId=%d, templates=%d",
                row.id(), eventRowId, row.notifications().size());

            try {
                var event = fetchEvent(eventRowId);
                if (event == null) {
                    log.infof("Row %d skipped: could not fetch event rowId=%d", row.id(), eventRowId);
                    continue;
                }

                log.infof("Row %d: event rawDateStart='%s'", row.id(), event.dateStart());
                Instant eventStart = com.lan.app.infrastructure.baserow.mapper.BaserowEventMapper.parseBaserowDate(event.dateStart());
                if (eventStart == null) {
                    log.infof("Row %d skipped: event rowId=%d has null dateStart", row.id(), eventRowId);
                    continue;
                }
                if (now.isAfter(eventStart)) {
                    log.infof("Row %d skipped: event rowId=%d already started (eventStart=%s, now=%s)",
                        row.id(), eventRowId, eventStart, now);
                    continue;
                }

                for (var notifLink : row.notifications()) {
                    int notifRowId = notifLink.id();
                    try {
                        var template = execute(() ->
                            notificationTemplateClient.getByRowId(notificationsTableId, notifRowId)
                        );
                        Duration leadTime = parseLeadTime(template.leadTime());
                        if (leadTime == null || template.message() == null) {
                            log.infof("Row %d, template %d skipped: leadTime=%s message=%s",
                                row.id(), notifRowId,
                                template.leadTime() != null ? template.leadTime().stream().map(s -> s.value()).toList() : null,
                                template.message());
                            continue;
                        }

                        Instant scheduledTime = eventStart.minus(leadTime);
                        log.infof("Row %d, template %d: eventStart=%s leadTime=%s scheduledTime=%s now=%s",
                            row.id(), notifRowId, eventStart,
                            template.leadTime().stream().map(s -> s.value()).toList(),
                            scheduledTime, now);

                        if (!isDue(scheduledTime, now)) {
                            log.infof("Row %d, template %d skipped: not due yet (scheduledTime=%s, window ends=%s)",
                                row.id(), notifRowId, scheduledTime,
                                scheduledTime.plus(MAX_OVERDUE_HOURS, ChronoUnit.HOURS));
                            continue;
                        }

                        List<NotificationRecipient> recipients = fetchRecipientsForEvent(eventRowId);
                        if (recipients.isEmpty()) {
                            log.infof("Row %d skipped: no Telegram chat IDs for event rowId=%d", row.id(), eventRowId);
                            continue;
                        }

                        log.infof("Row %d: sending notification to %d chat(s)", row.id(), recipients.size());
                        updateStatus(row.id(), "SENDING");
                        result.add(new EventNotificationDue(
                            row.id(),
                            applyPlaceholders(template.message(), event.name(), eventStart),
                            event.name(),
                            recipients
                        ));
                    } catch (Exception e) {
                        log.warnf("Failed to process template rowId=%d for events_notification rowId=%d: %s",
                            notifRowId, row.id(), e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.warnf("Failed to process events_notification rowId=%d: %s", row.id(), e.getMessage());
            }
        }

        log.infof("findDue: returning %d due notification(s)", result.size());
        return result;
    }

    @Override
    public void markSending(int rowId) {
        updateStatus(rowId, "SENDING");
    }

    @Override
    public void markSent(int rowId) {
        updateStatus(rowId, "SENT");
    }

    @Override
    public void markFailed(int rowId) {
        updateStatus(rowId, "FAILED");
    }

    private void updateStatus(int rowId, String status) {
        try {
            execute(() -> client.updateStatus(
                eventNotificationsTableId, rowId,
                new UpdateEventNotificationStatusRequest(status)
            ));
        } catch (Exception e) {
            log.warnf("Failed to update events_notification rowId=%d to status=%s: %s", rowId, status, e.getMessage());
        }
    }

    // Lets admins write one reusable template per lead time (e.g. "Напоминаем, что сегодня ({event_date})
    // встречаемся на «{event_name}» в LAN...") instead of hand-typing the name/date into every event's message.
    private String applyPlaceholders(String message, String eventName, Instant eventStart) {
        if (message == null) return null;
        String result = message.replace("{event_name}", eventName != null ? eventName : "");
        if (eventStart != null) {
            String eventDate = DateTimeFormatter.ofPattern("dd/MM").withZone(YEREVAN).format(eventStart);
            result = result.replace("{event_date}", eventDate);
        }
        return result;
    }

    private boolean isPending(BaserowEventNotificationRow row) {
        return row.status() != null && "pending".equalsIgnoreCase(row.status().value());
    }

    private Duration parseLeadTime(java.util.List<com.baserow.dto.BaserowSingleSelect> options) {
        if (options == null || options.isEmpty()) return null;
        String raw = options.getFirst().value();
        if (raw == null || raw.isBlank()) return null;
        String s = raw.trim().toLowerCase();
        try {
            if (s.endsWith("h")) return Duration.ofHours(Long.parseLong(s.substring(0, s.length() - 1).trim()));
            if (s.endsWith("m")) return Duration.ofMinutes(Long.parseLong(s.substring(0, s.length() - 1).trim()));
        } catch (NumberFormatException e) {
            log.warnf("Cannot parse lead_time value '%s'", raw);
        }
        return null;
    }

    private boolean isWorkingHour(ZonedDateTime time) {
        int hour = time.getHour();
        return hour >= workingHourStart && hour < workingHourEnd;
    }

    private boolean isDue(Instant scheduledTime, Instant now) {
        if (now.isBefore(scheduledTime)) return false;
        return now.isBefore(scheduledTime.plus(MAX_OVERDUE_HOURS, ChronoUnit.HOURS));
    }

    private BaserowEventRow fetchEvent(int eventRowId) {
        try {
            return execute(() -> eventClient.getByRowId(eventsTableId, eventRowId));
        } catch (Exception e) {
            log.warnf("Could not fetch event rowId=%d: %s", eventRowId, e.getMessage());
            return null;
        }
    }

    private List<NotificationRecipient> fetchRecipientsForEvent(int eventRowId) {
        var recipients = new ArrayList<NotificationRecipient>();
        try {
            var registrations = execute(() ->
                registrationClient.findByEventRowIdRaw(registrationsTableId, eventRowId).results()
            );
            for (var reg : registrations) {
                if (reg.guestId() == null || reg.guestId().isEmpty()) continue;
                int guestRowId = reg.guestId().getFirst().id();
                try {
                    var guest = execute(() -> guestClient.getByRowId(guestsTableId, guestRowId));
                    if (guest.telegramChatId() != null) {
                        recipients.add(new NotificationRecipient(guest.telegramChatId(), guestRowId, reg.id()));
                    }
                } catch (Exception e) {
                    log.warnf("Could not fetch guest rowId=%d: %s", guestRowId, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warnf("Could not fetch registrations for event rowId=%d: %s", eventRowId, e.getMessage());
        }
        return recipients;
    }

    @Override
    public void saveResults(int notificationRowId, List<NotificationResultRequest> results) {
        if (results == null || results.isEmpty()) return;
        String sentAt = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        boolean anyFailed = false;
        for (var r : results) {
            boolean failed = "FAILED".equalsIgnoreCase(r.status());
            if (failed) anyFailed = true;
            try {
                execute(() -> {
                    resultClient.create(notificationResultsTableId, new CreateEventNotificationResultRowRequest(
                        List.of(notificationRowId),
                        List.of(r.guestRowId()),
                        r.status(),
                        r.failureReason(),
                        sentAt,
                        List.of(r.registrationRowId())
                    ));
                    return null;
                });
            } catch (Exception e) {
                log.warnf("Failed to save notification result for guestRowId=%d: %s", r.guestRowId(), e.getMessage());
                anyFailed = true;
            }
        }
        updateStatus(notificationRowId, anyFailed ? "FAILED" : "SENT");
    }

    @Override
    public void recordGuestAction(int notificationRowId, int guestRowId, int registrationRowId, String action) {
        try {
            var existing = execute(() ->
                resultClient.findByNotificationAndGuestRaw(notificationResultsTableId, notificationRowId, guestRowId)
            ).results();

            if (existing.isEmpty()) {
                // No delivery-result row was created for this guest (e.g. it predates this feature) —
                // create one so the guest's answer still has somewhere to live.
                log.warnf("recordGuestAction: no result row for eventNotification=%d guest=%d, creating one",
                    notificationRowId, guestRowId);
                execute(() -> {
                    resultClient.create(notificationResultsTableId, new CreateEventNotificationResultRowRequest(
                        List.of(notificationRowId),
                        List.of(guestRowId),
                        "SENT",
                        null,
                        DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
                        List.of(registrationRowId)
                    ));
                    return null;
                });
                existing = execute(() ->
                    resultClient.findByNotificationAndGuestRaw(notificationResultsTableId, notificationRowId, guestRowId)
                ).results();
            }

            for (BaserowEventNotificationResultRow row : existing) {
                execute(() -> resultClient.updateAction(
                    notificationResultsTableId, row.id(), new UpdateNotificationResultActionRequest(action)
                ));
            }
        } catch (Exception e) {
            log.warnf(e, "Failed to record guest action for eventNotification=%d guest=%d: %s",
                notificationRowId, guestRowId, e.getMessage());
        }
    }
}
