package com.lan.app.infrastructure.baserow.repository;

import com.baserow.repository.AbstractBaserowRepository;
import com.lan.app.domain.model.EventNotificationDue;
import com.lan.app.infrastructure.baserow.client.BaserowEventClient;
import com.lan.app.infrastructure.baserow.client.BaserowEventNotificationClient;
import com.lan.app.infrastructure.baserow.client.BaserowEventRegistrationClient;
import com.lan.app.infrastructure.baserow.client.BaserowGuestClient;
import com.lan.app.infrastructure.baserow.client.BaserowNotificationTemplateClient;
import com.lan.app.infrastructure.baserow.dto.BaserowEventNotificationRow;
import com.lan.app.infrastructure.baserow.dto.BaserowEventRow;
import com.lan.app.infrastructure.baserow.dto.UpdateEventNotificationStatusRequest;
import com.lan.app.repository.repository.EventNotificationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class BaserowEventNotificationRepository extends AbstractBaserowRepository
        implements EventNotificationRepository {

    private static final Logger log = Logger.getLogger(BaserowEventNotificationRepository.class);
    private static final ZoneId YEREVAN = ZoneId.of("Asia/Yerevan");
    private static final int WORKING_HOUR_START = 9;
    private static final int WORKING_HOUR_END = 21;
    private static final long MAX_OVERDUE_HOURS = 24;

    private final int eventNotificationsTableId;
    private final int notificationsTableId;
    private final int eventsTableId;
    private final int registrationsTableId;
    private final int guestsTableId;

    private final BaserowEventNotificationClient client;
    private final BaserowNotificationTemplateClient notificationTemplateClient;
    private final BaserowEventClient eventClient;
    private final BaserowEventRegistrationClient registrationClient;
    private final BaserowGuestClient guestClient;

    public BaserowEventNotificationRepository(
        @ConfigProperty(name = "baserow.events.event-notifications-table-id") int eventNotificationsTableId,
        @ConfigProperty(name = "baserow.events.notifications-table-id") int notificationsTableId,
        @ConfigProperty(name = "baserow.events.events-table-id") int eventsTableId,
        @ConfigProperty(name = "baserow.events.registrations-table-id") int registrationsTableId,
        @ConfigProperty(name = "baserow.guests.guests-table-id") int guestsTableId,
        @RestClient BaserowEventNotificationClient client,
        @RestClient BaserowNotificationTemplateClient notificationTemplateClient,
        @RestClient BaserowEventClient eventClient,
        @RestClient BaserowEventRegistrationClient registrationClient,
        @RestClient BaserowGuestClient guestClient
    ) {
        this.eventNotificationsTableId = eventNotificationsTableId;
        this.notificationsTableId = notificationsTableId;
        this.eventsTableId = eventsTableId;
        this.registrationsTableId = registrationsTableId;
        this.guestsTableId = guestsTableId;
        this.client = client;
        this.notificationTemplateClient = notificationTemplateClient;
        this.eventClient = eventClient;
        this.registrationClient = registrationClient;
        this.guestClient = guestClient;
    }

    @Override
    public List<EventNotificationDue> findDue() {
        var rows = execute(() -> client.listActive(eventNotificationsTableId)).results();
        var now = Instant.now();
        var nowYerevan = ZonedDateTime.now(YEREVAN);

        if (!isWorkingHour(nowYerevan)) {
            log.debugf("Outside working hours (%d), skipping event notifications", nowYerevan.getHour());
            return List.of();
        }

        var result = new ArrayList<EventNotificationDue>();

        for (var row : rows) {
            if (!isPending(row)) continue;
            if (row.eventId() == null || row.eventId().isEmpty()) continue;
            if (row.notifications() == null || row.notifications().isEmpty()) continue;

            int eventRowId = row.eventId().getFirst().id();

            try {
                var event = fetchEvent(eventRowId);
                if (event == null) continue;

                Instant eventStart = event.dateStart();
                if (eventStart == null || now.isAfter(eventStart)) continue;

                for (var notifLink : row.notifications()) {
                    int notifRowId = notifLink.id();
                    try {
                        var template = execute(() ->
                            notificationTemplateClient.getByRowId(notificationsTableId, notifRowId)
                        );
                        if (template.leadHours() == null || template.message() == null) continue;

                        Instant scheduledTime = eventStart.minus(template.leadHours(), ChronoUnit.HOURS);
                        if (!isDue(scheduledTime, now)) continue;

                        List<Long> chatIds = fetchChatIdsForEvent(eventRowId);
                        if (chatIds.isEmpty()) {
                            log.debugf("No Telegram chat IDs for event rowId=%d, skipping", eventRowId);
                            continue;
                        }

                        // Mark as sending before returning so the next scheduler tick skips this row
                        updateStatus(row.id(), "sending");
                        result.add(new EventNotificationDue(
                            row.id(),
                            template.message(),
                            event.name(),
                            chatIds
                        ));
                        break; // one notification per events_notification row per run
                    } catch (Exception e) {
                        log.warnf("Failed to process template rowId=%d for events_notification rowId=%d: %s",
                            notifRowId, row.id(), e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.warnf("Failed to process events_notification rowId=%d: %s", row.id(), e.getMessage());
            }
        }

        return result;
    }

    @Override
    public void markSending(int rowId) {
        updateStatus(rowId, "sending");
    }

    @Override
    public void markSent(int rowId) {
        updateStatus(rowId, "sent");
    }

    @Override
    public void markFailed(int rowId) {
        updateStatus(rowId, "failed");
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

    private boolean isPending(BaserowEventNotificationRow row) {
        return row.status() != null && "pending".equalsIgnoreCase(row.status().value());
    }

    private boolean isWorkingHour(ZonedDateTime time) {
        int hour = time.getHour();
        return hour >= WORKING_HOUR_START && hour < WORKING_HOUR_END;
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

    private List<Long> fetchChatIdsForEvent(int eventRowId) {
        var chatIds = new ArrayList<Long>();
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
                        chatIds.add(guest.telegramChatId());
                    }
                } catch (Exception e) {
                    log.warnf("Could not fetch guest rowId=%d: %s", guestRowId, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warnf("Could not fetch registrations for event rowId=%d: %s", eventRowId, e.getMessage());
        }
        return chatIds;
    }
}
