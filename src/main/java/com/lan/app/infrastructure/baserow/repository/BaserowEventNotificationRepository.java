package com.lan.app.infrastructure.baserow.repository;

import com.baserow.repository.AbstractBaserowRepository;
import com.lan.app.api.dto.request.NotificationResultRequest;
import com.lan.app.domain.model.EventNotificationDue;
import com.lan.app.domain.model.EventNotificationPreview;
import com.lan.app.domain.model.EventSurveyDue;
import com.lan.app.domain.model.NotificationRecipient;
import com.lan.app.infrastructure.baserow.client.BaserowEventClient;
import com.lan.app.infrastructure.baserow.client.BaserowEventNotificationClient;
import com.lan.app.infrastructure.baserow.client.BaserowEventNotificationResultClient;
import com.lan.app.infrastructure.baserow.client.BaserowEventRegistrationClient;
import com.lan.app.infrastructure.baserow.client.BaserowGuestClient;
import com.lan.app.infrastructure.baserow.dto.BaserowEventRow;
import com.lan.app.infrastructure.baserow.dto.BaserowEventNotificationResultRow;
import com.lan.app.infrastructure.baserow.dto.BaserowRegistrationRow;
import com.lan.app.infrastructure.baserow.dto.CreateEventNotificationResultRowRequest;
import com.lan.app.infrastructure.baserow.dto.CreateEventNotificationRowRequest;
import com.lan.app.infrastructure.baserow.dto.UpdateEventNotificationStatusRequest;
import com.lan.app.infrastructure.baserow.dto.UpdateNotificationResultActionRequest;
import com.lan.app.infrastructure.baserow.dto.UpdateSurveySentRequest;
import com.lan.app.infrastructure.baserow.mapper.BaserowEventMapper;
import com.lan.app.repository.repository.EventNotificationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
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

    // Fixed reminder policy — no longer admin-configurable. Wave A covers everyone registered
    // in time for the day-before reminder; guests who register after that cutoff (but before the
    // day-of cutoff) get wave B instead; anyone registering later than wave B gets nothing
    // automated and is followed up manually.
    private static final double WAVE_A_SEND_TIME_SECONDS = 14 * 3600.0;              // day before, 14:00 Yerevan
    private static final double WAVE_B_SEND_TIME_SECONDS = 10 * 3600.0 + 15 * 60.0;  // day of, 10:15 Yerevan

    // Post-event feedback survey — sent only to guests who confirmed attendance (action=CONFIRMED
    // on their notification-result row), one day after the event, 14:00 Yerevan.
    private static final int SURVEY_OFFSET_DAYS = 1;
    private static final double SURVEY_SEND_TIME_SECONDS = 14 * 3600.0;              // day after, 14:00 Yerevan
    private static final String CONFIRMED_ACTION = "CONFIRMED";
    // Wave A fires the day before the event, so its copy must say "завтра"/"tomorrow"; wave B
    // fires the day of the event and keeps "сегодня"/"today". Previously both waves shared one
    // template hardcoded to "today", so day-before reminders wrongly claimed the event was today.
    private static final String MESSAGE_RU_TOMORROW = "Привет! ⚡️ Напоминаем, что завтра ({event_date}) встречаемся на «{event_name}» в LAN. Подтвердите, пожалуйста, ваше участие, чтобы мы правильно рассчитали количество мест:";
    private static final String MESSAGE_EN_TOMORROW = "Hi! ⚡️ Just a reminder that tomorrow ({event_date}) we're meeting for \"{event_name}\" at LAN. Please confirm your participation so we can accurately calculate the number of seats:";
    private static final String MESSAGE_RU_TODAY = "Привет! ⚡️ Напоминаем, что сегодня ({event_date}) встречаемся на «{event_name}» в LAN. Подтвердите, пожалуйста, ваше участие, чтобы мы правильно рассчитали количество мест:";
    private static final String MESSAGE_EN_TODAY = "Hi! ⚡️ Just a reminder that today ({event_date}) we're meeting for \"{event_name}\" at LAN. Please confirm your participation so we can accurately calculate the number of seats:";

    private final int eventNotificationsTableId;
    private final int eventsTableId;
    private final int registrationsTableId;
    private final int guestsTableId;
    private final int workingHourStart;
    private final int workingHourEnd;

    private final int notificationResultsTableId;

    private final BaserowEventNotificationClient client;
    private final BaserowEventClient eventClient;
    private final BaserowEventRegistrationClient registrationClient;
    private final BaserowGuestClient guestClient;
    private final BaserowEventNotificationResultClient resultClient;

    // Overridable in tests (package-private setter below) to pin "now" to a fixed instant —
    // production always uses the real clock in the Yerevan zone.
    private Clock clock = Clock.system(YEREVAN);

    public BaserowEventNotificationRepository(
        @ConfigProperty(name = "baserow.events.event-notifications-table-id") int eventNotificationsTableId,
        @ConfigProperty(name = "baserow.events.events-table-id") int eventsTableId,
        @ConfigProperty(name = "baserow.events.registrations-table-id") int registrationsTableId,
        @ConfigProperty(name = "baserow.guests.guests-table-id") int guestsTableId,
        @ConfigProperty(name = "baserow.events.notification-results-table-id") int notificationResultsTableId,
        @ConfigProperty(name = "app.notifications.working-hour-start", defaultValue = "9") int workingHourStart,
        @ConfigProperty(name = "app.notifications.working-hour-end", defaultValue = "21") int workingHourEnd,
        @RestClient BaserowEventNotificationClient client,
        @RestClient BaserowEventClient eventClient,
        @RestClient BaserowEventRegistrationClient registrationClient,
        @RestClient BaserowGuestClient guestClient,
        @RestClient BaserowEventNotificationResultClient resultClient
    ) {
        this.eventNotificationsTableId = eventNotificationsTableId;
        this.eventsTableId = eventsTableId;
        this.registrationsTableId = registrationsTableId;
        this.guestsTableId = guestsTableId;
        this.notificationResultsTableId = notificationResultsTableId;
        this.workingHourStart = workingHourStart;
        this.workingHourEnd = workingHourEnd;
        this.client = client;
        this.eventClient = eventClient;
        this.registrationClient = registrationClient;
        this.guestClient = guestClient;
        this.resultClient = resultClient;
    }

    void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public List<EventNotificationDue> findDue() {
        var now = Instant.now(clock);
        var nowYerevan = ZonedDateTime.now(clock);

        if (!isWorkingHour(nowYerevan)) {
            log.infof("Outside working hours (%d), skipping event notifications", nowYerevan.getHour());
            return List.of();
        }

        var events = execute(() -> eventClient.list(eventsTableId)).results();
        log.infof("findDue: scanning %d event(s), now=%s (Yerevan hour=%d)", events.size(), now, nowYerevan.getHour());

        var result = new ArrayList<EventNotificationDue>();

        for (var event : events) {
            Instant eventStart = BaserowEventMapper.parseBaserowDate(event.dateStart());
            if (eventStart == null || now.isAfter(eventStart)) continue;

            Instant waveA = computeScheduledTime(eventStart, 1, WAVE_A_SEND_TIME_SECONDS);
            Instant waveB = computeScheduledTime(eventStart, 0, WAVE_B_SEND_TIME_SECONDS);
            boolean waveADue = isDue(waveA, now);
            boolean waveBDue = isDue(waveB, now);
            if (!waveADue && !waveBDue) continue;

            int eventRowId = event.id();
            try {
                if (waveADue) {
                    addWaveNotification(result, eventRowId, event, eventStart, waveA, waveB, now,
                        waveA, MESSAGE_EN_TOMORROW, MESSAGE_RU_TOMORROW);
                }
                if (waveBDue) {
                    addWaveNotification(result, eventRowId, event, eventStart, waveA, waveB, now,
                        waveB, MESSAGE_EN_TODAY, MESSAGE_RU_TODAY);
                }
            } catch (Exception e) {
                log.warnf("Failed to process event rowId=%d: %s", eventRowId, e.getMessage());
            }
        }

        log.infof("findDue: returning %d due notification(s)", result.size());
        return result;
    }

    // Resolves recipients for one specific wave and, if any are found, appends the corresponding
    // EventNotificationDue (with wording matching that wave) to the result list.
    private void addWaveNotification(List<EventNotificationDue> result, int eventRowId, BaserowEventRow event,
            Instant eventStart, Instant waveA, Instant waveB, Instant now, Instant targetWave,
            String messageEnTemplate, String messageRuTemplate) {
        var recipients = collectEligibleRecipients(eventRowId, waveA, waveB, now, targetWave);
        if (recipients.isEmpty()) return;

        int anchorRowId = findOrCreateAnchorRow(eventRowId);
        log.infof("Event %d: sending notification to %d chat(s) via events_notification row %d",
            eventRowId, recipients.size(), anchorRowId);

        result.add(new EventNotificationDue(
            anchorRowId,
            applyPlaceholders(messageEnTemplate, event.name(), eventStart),
            applyPlaceholders(messageRuTemplate, event.name(), eventStart),
            event.name(),
            recipients
        ));
    }

    // For each registration on the event, figures out which wave (if any) it's assigned to —
    // the earliest wave that's strictly after the guest registered — and, if that wave is due
    // now, matches targetWave, and the guest hasn't already been notified for this event,
    // resolves their chat id.
    private List<NotificationRecipient> collectEligibleRecipients(
            int eventRowId, Instant waveA, Instant waveB, Instant now, Instant targetWave) {
        var recipients = new ArrayList<NotificationRecipient>();
        // A guest can have more than one registration row for the same event (nothing prevents
        // re-registering) — without this, two eligible registrations for the same guest would
        // both pass the alreadyNotified() check (it only reflects past ticks' Baserow writes,
        // not sends queued earlier in this very pass) and the guest would get double-messaged.
        var seenGuestRowIds = new java.util.HashSet<Integer>();
        Integer anchorRowId = null;

        List<BaserowRegistrationRow> registrations;
        try {
            registrations = execute(() -> registrationClient.findByEventRowIdRaw(registrationsTableId, eventRowId).results());
        } catch (Exception e) {
            log.warnf("Could not fetch registrations for event rowId=%d: %s", eventRowId, e.getMessage());
            return recipients;
        }

        for (var reg : registrations) {
            if (Boolean.TRUE.equals(reg.isCancelled())) continue;
            if (reg.guestId() == null || reg.guestId().isEmpty()) continue;

            Instant registeredAt = BaserowEventMapper.parseBaserowDate(reg.createdAt());
            if (registeredAt == null) registeredAt = Instant.EPOCH;

            Instant assigned = assignedWave(registeredAt, waveA, waveB);
            if (assigned == null || !assigned.equals(targetWave) || !isDue(assigned, now)) continue;

            int guestRowId = reg.guestId().getFirst().id();
            if (!seenGuestRowIds.add(guestRowId)) continue;

            try {
                if (anchorRowId == null) anchorRowId = findOrCreateAnchorRow(eventRowId);
                if (alreadyNotified(anchorRowId, guestRowId)) continue;

                var guest = execute(() -> guestClient.getByRowId(guestsTableId, guestRowId));
                if (guest.telegramChatId() != null) {
                    recipients.add(new NotificationRecipient(guest.telegramChatId(), guestRowId, reg.id()));
                }
            } catch (Exception e) {
                log.warnf("Could not resolve guest rowId=%d for event rowId=%d: %s", guestRowId, eventRowId, e.getMessage());
            }
        }
        return recipients;
    }

    // Earliest of {waveA, waveB} that lies strictly after the registration timestamp; null if
    // the guest registered after both (no automated reminder — manual follow-up territory).
    private Instant assignedWave(Instant registeredAt, Instant waveA, Instant waveB) {
        Instant assigned = null;
        if (waveA != null && waveA.isAfter(registeredAt)) assigned = waveA;
        if (waveB != null && waveB.isAfter(registeredAt) && (assigned == null || waveB.isBefore(assigned))) assigned = waveB;
        return assigned;
    }

    private boolean alreadyNotified(int anchorRowId, int guestRowId) {
        try {
            return !execute(() ->
                resultClient.findByNotificationAndGuestRaw(notificationResultsTableId, anchorRowId, guestRowId)
            ).results().isEmpty();
        } catch (Exception e) {
            log.warnf("Could not check existing notification results for anchor=%d guest=%d: %s",
                anchorRowId, guestRowId, e.getMessage());
            return false;
        }
    }

    private int findOrCreateAnchorRow(int eventRowId) {
        var existing = execute(() -> client.findByEventIdRaw(eventNotificationsTableId, eventRowId)).results();
        if (!existing.isEmpty()) return existing.getFirst().id();

        var created = execute(() -> client.create(
            eventNotificationsTableId,
            new CreateEventNotificationRowRequest(List.of(eventRowId), true)
        ));
        log.infof("Auto-created events_notification anchor row %d for event rowId=%d", created.id(), eventRowId);
        return created.id();
    }

    // Read-only counterpart of findDue() for the website's in-app notification channel. Not
    // guest-specific (the caller doesn't identify which registration is asking) — just reports
    // whether either wave is currently due for the event.
    @Override
    public List<EventNotificationPreview> findDueForEvent(int eventRowId) {
        var now = Instant.now(clock);
        var nowYerevan = ZonedDateTime.now(clock);
        if (!isWorkingHour(nowYerevan)) {
            return List.of();
        }

        var event = fetchEvent(eventRowId);
        if (event == null) {
            return List.of();
        }
        Instant eventStart = BaserowEventMapper.parseBaserowDate(event.dateStart());
        if (eventStart == null || now.isAfter(eventStart)) {
            return List.of();
        }

        Instant waveA = computeScheduledTime(eventStart, 1, WAVE_A_SEND_TIME_SECONDS);
        Instant waveB = computeScheduledTime(eventStart, 0, WAVE_B_SEND_TIME_SECONDS);
        boolean waveADue = isDue(waveA, now);
        if (!waveADue && !isDue(waveB, now)) {
            return List.of();
        }

        // If wave A (day-before) is due, prefer "tomorrow" wording even if wave B also happens
        // to be due at the same instant — matches which wave findDue() would act on first.
        String messageEnTemplate = waveADue ? MESSAGE_EN_TOMORROW : MESSAGE_EN_TODAY;
        String messageRuTemplate = waveADue ? MESSAGE_RU_TOMORROW : MESSAGE_RU_TODAY;

        return List.of(new EventNotificationPreview(
            eventRowId,
            applyPlaceholders(messageEnTemplate, event.name(), eventStart),
            applyPlaceholders(messageRuTemplate, event.name(), eventStart),
            event.name()
        ));
    }

    // One day after an event, surveys guests who confirmed attendance (action=CONFIRMED on their
    // notification-result row from the reminder flow) — the only signal of intent-to-attend this
    // system has, real check-in data doesn't exist. Idempotency: survey_sent is patched to true on
    // the same result row as soon as a recipient is returned here, mirroring how
    // event-capacity-alerts/due marks itself immediately rather than requiring a separate mark-sent
    // call.
    @Override
    public List<EventSurveyDue> findSurveyDue() {
        var now = Instant.now(clock);
        var nowYerevan = ZonedDateTime.now(clock);

        if (!isWorkingHour(nowYerevan)) {
            return List.of();
        }

        var events = execute(() -> eventClient.list(eventsTableId)).results();
        var result = new ArrayList<EventSurveyDue>();

        for (var event : events) {
            Instant eventStart = BaserowEventMapper.parseBaserowDate(event.dateStart());
            if (eventStart == null) continue;

            Instant surveyTime = computeScheduledTimeAfter(eventStart, SURVEY_OFFSET_DAYS, SURVEY_SEND_TIME_SECONDS);
            if (!isDue(surveyTime, now)) continue;

            try {
                result.addAll(collectSurveyRecipients(event.id(), event.name(), now));
            } catch (Exception e) {
                log.warnf("Failed to process survey for event rowId=%d: %s", event.id(), e.getMessage());
            }
        }

        return result;
    }

    // Only guests with a CONFIRMED result row for this event's anchor notification, whose survey
    // hasn't already been sent. If no anchor row exists, reminders were never sent for this event,
    // so no CONFIRMED signal is possible — the whole event is skipped.
    private List<EventSurveyDue> collectSurveyRecipients(int eventRowId, String eventName, Instant now) {
        var result = new ArrayList<EventSurveyDue>();

        Integer anchorRowId = findExistingAnchorRow(eventRowId);
        if (anchorRowId == null) return result;

        List<BaserowRegistrationRow> registrations;
        try {
            registrations = execute(() -> registrationClient.findByEventRowIdRaw(registrationsTableId, eventRowId).results());
        } catch (Exception e) {
            log.warnf("Could not fetch registrations for event rowId=%d: %s", eventRowId, e.getMessage());
            return result;
        }

        var seenGuestRowIds = new java.util.HashSet<Integer>();

        for (var reg : registrations) {
            if (Boolean.TRUE.equals(reg.isCancelled())) continue;
            if (reg.guestId() == null || reg.guestId().isEmpty()) continue;

            int guestRowId = reg.guestId().getFirst().id();
            if (!seenGuestRowIds.add(guestRowId)) continue;

            try {
                var resultRows = execute(() ->
                    resultClient.findByNotificationAndGuestRaw(notificationResultsTableId, anchorRowId, guestRowId)
                ).results();

                var confirmedRow = resultRows.stream()
                    .filter(r -> r.action() != null && CONFIRMED_ACTION.equals(r.action().value()))
                    .filter(r -> !Boolean.TRUE.equals(r.surveySent()))
                    .findFirst();
                if (confirmedRow.isEmpty()) continue;

                var guest = execute(() -> guestClient.getByRowId(guestsTableId, guestRowId));
                if (guest.telegramChatId() == null) continue;

                execute(() -> resultClient.updateSurveySent(
                    notificationResultsTableId, confirmedRow.get().id(), new UpdateSurveySentRequest(true)
                ));

                result.add(new EventSurveyDue(eventRowId, eventName, guestRowId, reg.id(), guest.telegramChatId()));
            } catch (Exception e) {
                log.warnf("Could not resolve survey recipient guestRowId=%d for event rowId=%d: %s",
                    guestRowId, eventRowId, e.getMessage());
            }
        }
        return result;
    }

    private Integer findExistingAnchorRow(int eventRowId) {
        var existing = execute(() -> client.findByEventIdRaw(eventNotificationsTableId, eventRowId)).results();
        return existing.isEmpty() ? null : existing.getFirst().id();
    }

    // offsetDays counts calendar days after the event's Yerevan-local date (1 = day after);
    // sendTimeSeconds is the Yerevan-local time of day to send, as seconds since midnight.
    private Instant computeScheduledTimeAfter(Instant eventStart, int offsetDays, double sendTimeSeconds) {
        long secondsOfDay = (long) sendTimeSeconds % 86400;
        LocalTime sendTime = LocalTime.ofSecondOfDay(secondsOfDay);
        LocalDate targetDate = eventStart.atZone(YEREVAN).toLocalDate().plusDays(offsetDays);
        return ZonedDateTime.of(targetDate, sendTime, YEREVAN).toInstant();
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

    private String applyPlaceholders(String message, String eventName, Instant eventStart) {
        if (message == null) return null;
        String result = message.replace("{event_name}", eventName != null ? eventName : "");
        if (eventStart != null) {
            String eventDate = DateTimeFormatter.ofPattern("dd/MM").withZone(YEREVAN).format(eventStart);
            result = result.replace("{event_date}", eventDate);
        }
        return result;
    }

    // offsetDays counts calendar days before the event's Yerevan-local date (0 = day of the event);
    // sendTimeSeconds is the Yerevan-local time of day to send, as seconds since midnight.
    private Instant computeScheduledTime(Instant eventStart, Integer offsetDays, Double sendTimeSeconds) {
        if (offsetDays == null || offsetDays < 0 || sendTimeSeconds == null) return null;
        long secondsOfDay = sendTimeSeconds.longValue() % 86400;
        LocalTime sendTime = LocalTime.ofSecondOfDay(secondsOfDay);
        LocalDate targetDate = eventStart.atZone(YEREVAN).toLocalDate().minusDays(offsetDays);
        return ZonedDateTime.of(targetDate, sendTime, YEREVAN).toInstant();
    }

    private boolean isWorkingHour(ZonedDateTime time) {
        int hour = time.getHour();
        return hour >= workingHourStart && hour < workingHourEnd;
    }

    private boolean isDue(Instant scheduledTime, Instant now) {
        if (scheduledTime == null) return false;
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
