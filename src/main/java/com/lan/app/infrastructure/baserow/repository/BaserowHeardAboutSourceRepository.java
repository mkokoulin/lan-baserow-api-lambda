package com.lan.app.infrastructure.baserow.repository;

import com.baserow.repository.AbstractBaserowRepository;
import com.lan.app.domain.model.HeardAboutSourceRecipient;
import com.lan.app.infrastructure.baserow.client.BaserowGuestClient;
import com.lan.app.infrastructure.baserow.dto.UpdateHeardAboutSourceRequest;
import com.lan.app.infrastructure.baserow.mapper.BaserowEventMapper;
import com.lan.app.repository.HeardAboutSourceRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

// Surveys guests one day after their Guests-table row was created (see docs/heard-about-source-field.md).
// Idempotency: heard_about_survey_sent is patched to true on the guest row the instant it's
// returned from findDue(), mirroring how event-capacity-alerts/due and event-surveys/due mark
// themselves immediately rather than requiring a separate mark-sent call. The 5-minute poll
// interval combined with the isDue()/isWorkingHour() gate is what defers a guest whose 24h mark
// falls outside working hours to the next opening, without any extra "reschedule" logic.
@ApplicationScoped
public class BaserowHeardAboutSourceRepository extends AbstractBaserowRepository
        implements HeardAboutSourceRepository {

    private static final Logger log = Logger.getLogger(BaserowHeardAboutSourceRepository.class);
    private static final ZoneId YEREVAN = ZoneId.of("Asia/Yerevan");
    private static final long SURVEY_DELAY_HOURS = 24;
    private static final long MAX_OVERDUE_HOURS = 24;

    private final int guestsTableId;
    private final int weekdayHourStart;
    private final int weekdayHourEnd;
    private final int weekendHourStart;
    private final int weekendHourEnd;
    private final BaserowGuestClient guestClient;

    // Overridable in tests (package-private setter below) to pin "now" to a fixed instant —
    // production always uses the real clock in the Yerevan zone.
    private Clock clock = Clock.system(YEREVAN);

    public BaserowHeardAboutSourceRepository(
        @ConfigProperty(name = "baserow.guests.guests-table-id") int guestsTableId,
        @ConfigProperty(name = "app.notifications.heard-about.weekday-start", defaultValue = "10") int weekdayHourStart,
        @ConfigProperty(name = "app.notifications.heard-about.weekday-end", defaultValue = "22") int weekdayHourEnd,
        @ConfigProperty(name = "app.notifications.heard-about.weekend-start", defaultValue = "10") int weekendHourStart,
        @ConfigProperty(name = "app.notifications.heard-about.weekend-end", defaultValue = "16") int weekendHourEnd,
        @RestClient BaserowGuestClient guestClient
    ) {
        this.guestsTableId = guestsTableId;
        this.weekdayHourStart = weekdayHourStart;
        this.weekdayHourEnd = weekdayHourEnd;
        this.weekendHourStart = weekendHourStart;
        this.weekendHourEnd = weekendHourEnd;
        this.guestClient = guestClient;
    }

    void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public List<HeardAboutSourceRecipient> findDue() {
        var now = Instant.now(clock);
        var nowYerevan = ZonedDateTime.now(clock);
        if (!isWorkingHour(nowYerevan)) {
            return List.of();
        }

        var guests = execute(() -> guestClient.listAllRaw(guestsTableId)).results();
        var result = new ArrayList<HeardAboutSourceRecipient>();

        for (var guest : guests) {
            if (guest.telegramChatId() == null) continue;
            if (Boolean.TRUE.equals(guest.heardAboutSurveySent())) continue;

            Instant createdAt = BaserowEventMapper.parseBaserowDate(guest.createdAt());
            if (createdAt == null) continue;

            Instant scheduledTime = createdAt.plus(SURVEY_DELAY_HOURS, ChronoUnit.HOURS);
            if (!isDue(scheduledTime, now)) continue;

            try {
                execute(() -> guestClient.patchHeardAboutSource(
                    guestsTableId, guest.id(), new UpdateHeardAboutSourceRequest(null, null, true)
                ));
                result.add(new HeardAboutSourceRecipient(guest.id(), guest.telegramChatId()));
            } catch (Exception e) {
                log.warnf("Could not mark heard-about survey sent for guestRowId=%d: %s", guest.id(), e.getMessage());
            }
        }

        return result;
    }

    @Override
    public void saveAnswer(int guestRowId, String source, String comment) {
        execute(() -> guestClient.patchHeardAboutSource(
            guestsTableId, guestRowId, new UpdateHeardAboutSourceRequest(source, comment, true)
        ));
    }

    private boolean isWorkingHour(ZonedDateTime time) {
        boolean weekend = time.getDayOfWeek() == DayOfWeek.SATURDAY || time.getDayOfWeek() == DayOfWeek.SUNDAY;
        int start = weekend ? weekendHourStart : weekdayHourStart;
        int end = weekend ? weekendHourEnd : weekdayHourEnd;
        int hour = time.getHour();
        return hour >= start && hour < end;
    }

    private boolean isDue(Instant scheduledTime, Instant now) {
        if (scheduledTime == null) return false;
        if (now.isBefore(scheduledTime)) return false;
        return now.isBefore(scheduledTime.plus(MAX_OVERDUE_HOURS, ChronoUnit.HOURS));
    }
}
