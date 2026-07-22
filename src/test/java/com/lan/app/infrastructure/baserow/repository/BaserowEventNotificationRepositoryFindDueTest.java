package com.lan.app.infrastructure.baserow.repository;

import com.baserow.dto.BaserowLinkToTable;
import com.baserow.dto.BaserowListResponse;
import com.lan.app.infrastructure.baserow.client.BaserowEventClient;
import com.lan.app.infrastructure.baserow.client.BaserowEventNotificationClient;
import com.lan.app.infrastructure.baserow.client.BaserowEventNotificationResultClient;
import com.lan.app.infrastructure.baserow.client.BaserowEventRegistrationClient;
import com.lan.app.infrastructure.baserow.client.BaserowGuestClient;
import com.lan.app.infrastructure.baserow.dto.BaserowEventNotificationResultRow;
import com.lan.app.infrastructure.baserow.dto.BaserowEventNotificationRow;
import com.lan.app.infrastructure.baserow.dto.BaserowEventRow;
import com.lan.app.infrastructure.baserow.dto.BaserowGuestRow;
import com.lan.app.infrastructure.baserow.dto.BaserowRegistrationRow;
import com.lan.app.infrastructure.baserow.dto.CreateEventNotificationRowRequest;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Covers the fixed two-wave reminder policy: everyone registered before 14:00 the day before
 * gets wave A; late registrants (up to 10:15 the day of) get wave B instead; anyone later gets
 * nothing automated. "Now" is pinned via the package-private {@code setClock} test hook so the
 * timing math is deterministic regardless of when the test actually runs.
 */
@QuarkusTest
@DisplayName("BaserowEventNotificationRepository — findDue()")
class BaserowEventNotificationRepositoryFindDueTest {

    static final int EVENTS_TABLE = 992074;
    static final int REGISTRATIONS_TABLE = 992071;
    static final int GUESTS_TABLE = 824729;
    static final int EVENT_NOTIFICATIONS_TABLE = 831434;
    static final int RESULTS_TABLE = 1042307;
    static final ZoneId YEREVAN = ZoneId.of("Asia/Yerevan");

    static final int EVENT_ROW_ID = 4242;
    static final int ANCHOR_ROW_ID = 900;

    // Event happens 2026-07-25 at 19:00 Yerevan.
    static final ZonedDateTime EVENT_START = ZonedDateTime.of(2026, 7, 25, 19, 0, 0, 0, YEREVAN);
    // Wave A: day before, 14:00 -> 2026-07-24T14:00
    // Wave B: day of, 10:15     -> 2026-07-25T10:15

    @Inject
    BaserowEventNotificationRepository repo;

    @InjectMock
    @RestClient
    BaserowEventClient eventClient;

    @InjectMock
    @RestClient
    BaserowEventRegistrationClient registrationClient;

    @InjectMock
    @RestClient
    BaserowGuestClient guestClient;

    @InjectMock
    @RestClient
    BaserowEventNotificationClient notificationClient;

    @InjectMock
    @RestClient
    BaserowEventNotificationResultClient resultClient;

    private void pinNow(ZonedDateTime now) {
        repo.setClock(Clock.fixed(now.toInstant(), YEREVAN));
    }

    private static String iso(ZonedDateTime zdt) {
        return DateTimeFormatter.ISO_INSTANT.format(zdt.toInstant());
    }

    private static BaserowEventRow event() {
        return new BaserowEventRow(
            EVENT_ROW_ID, UUID.randomUUID(), "LAN Party",
            iso(EVENT_START), null, "desc", null, URI.create("https://example.com"),
            null, null, true, List.of(), null, 1, true, true, false, null, List.of(), null
        );
    }

    private static BaserowRegistrationRow registration(int id, int guestRowId, ZonedDateTime createdAt) {
        return new BaserowRegistrationRow(
            id, UUID.randomUUID(),
            List.of(new BaserowLinkToTable(EVENT_ROW_ID, "e")),
            List.of(new BaserowLinkToTable(guestRowId, "g")),
            iso(createdAt), 1, "", null, false
        );
    }

    private static BaserowGuestRow guest(int rowId, Long chatId) {
        return new BaserowGuestRow(rowId, UUID.randomUUID(), "Guest", String.valueOf(rowId), "+374", null, chatId, null);
    }

    private static <T> BaserowListResponse<T> listOf(T... items) {
        return new BaserowListResponse<>(items.length, null, null, List.of(items));
    }

    private void stubEvents(BaserowEventRow... events) {
        // BaserowEventClient.list() is a default method that delegates to listAll() —
        // stub the latter so the real filtering logic still runs against our mock data.
        when(eventClient.listAll(EVENTS_TABLE)).thenReturn(listOf(events));
    }

    private void stubRegistrations(BaserowRegistrationRow... regs) {
        when(registrationClient.findByEventRowIdRaw(REGISTRATIONS_TABLE, EVENT_ROW_ID)).thenReturn(listOf(regs));
    }

    private void stubGuest(int guestRowId, Long chatId) {
        when(guestClient.getByRowId(GUESTS_TABLE, guestRowId)).thenReturn(guest(guestRowId, chatId));
    }

    private void stubAlreadyNotified(int guestRowId, boolean already) {
        when(resultClient.findByNotificationAndGuestRaw(RESULTS_TABLE, ANCHOR_ROW_ID, guestRowId))
            .thenReturn(already
                ? listOf(new BaserowEventNotificationResultRow(1, List.of(), List.of(), null, null, null, null))
                : listOf());
    }

    private void stubNoExistingAnchor() {
        when(notificationClient.findByEventIdRaw(EVENT_NOTIFICATIONS_TABLE, EVENT_ROW_ID)).thenReturn(listOf());
        when(notificationClient.create(eq(EVENT_NOTIFICATIONS_TABLE), any())).thenReturn(
            new BaserowEventNotificationRow(ANCHOR_ROW_ID, UUID.randomUUID(), null, List.of(),
                List.of(new BaserowLinkToTable(EVENT_ROW_ID, "e")), true)
        );
    }

    private void stubExistingAnchor() {
        when(notificationClient.findByEventIdRaw(EVENT_NOTIFICATIONS_TABLE, EVENT_ROW_ID)).thenReturn(
            listOf(new BaserowEventNotificationRow(ANCHOR_ROW_ID, UUID.randomUUID(), null, List.of(),
                List.of(new BaserowLinkToTable(EVENT_ROW_ID, "e")), true))
        );
    }

    @Nested
    @DisplayName("wave A due (day before, 14:00)")
    class WaveADue {

        @Test
        @DisplayName("early registrant with a chat id and no prior result -> notified; late/no-chat-id/already-notified are skipped")
        void notifiesOnlyEligibleEarlyRegistrant() {
            pinNow(EVENT_START.minusDays(1).withHour(15).withMinute(0)); // 1h after wave A, well before wave B

            stubEvents(event());
            stubNoExistingAnchor();

            var early = registration(1, 101, EVENT_START.minusDays(4));                 // before wave A -> wave A
            var lateForWaveB = registration(2, 102, EVENT_START.minusDays(1).withHour(16)); // after wave A, before wave B -> wave B (not due yet)
            var tooLate = registration(3, 103, EVENT_START.withHour(11));               // after wave B -> never
            var alreadyNotified = registration(4, 104, EVENT_START.minusDays(4));       // eligible for wave A but already sent
            var noChatId = registration(5, 105, EVENT_START.minusDays(4));              // eligible for wave A but no telegram chat

            stubRegistrations(early, lateForWaveB, tooLate, alreadyNotified, noChatId);

            stubGuest(101, 555101L);
            stubGuest(102, 555102L);
            stubGuest(103, 555103L);
            stubGuest(104, 555104L);
            stubGuest(105, null);

            stubAlreadyNotified(101, false);
            stubAlreadyNotified(104, true);
            stubAlreadyNotified(105, false);

            var due = repo.findDue();

            assertEquals(1, due.size());
            var notification = due.get(0);
            assertEquals(ANCHOR_ROW_ID, notification.rowId());
            assertThat(notification.recipients(), hasSize(1));
            assertEquals(555101L, notification.recipients().get(0).chatId());
            assertEquals(101, notification.recipients().get(0).guestRowId());
            assertThat(notification.messageRu(), containsString("LAN Party"));
            assertThat(notification.messageRu(), containsString("25/07")); // {event_date} is the event's date, not the send date

            verify(guestClient, never()).getByRowId(GUESTS_TABLE, 102);
            verify(guestClient, never()).getByRowId(GUESTS_TABLE, 103);
        }
    }

    @Nested
    @DisplayName("wave B due (day of, 10:15)")
    class WaveBDue {

        @Test
        @DisplayName("late registrant now gets notified; early registrant already handled by wave A is skipped via dedup; too-late registrant never notified")
        void notifiesOnlyLateRegistrant() {
            pinNow(EVENT_START.withHour(10).withMinute(30)); // 15min after wave B

            stubEvents(event());
            stubExistingAnchor(); // anchor row already created during wave A

            var early = registration(1, 101, EVENT_START.minusDays(4));                 // wave A, already notified
            var late = registration(2, 102, EVENT_START.minusDays(1).withHour(16));     // wave B, not yet notified
            var tooLate = registration(3, 103, EVENT_START.withHour(11));               // after wave B -> never

            stubRegistrations(early, late, tooLate);

            stubGuest(101, 555101L);
            stubGuest(102, 555102L);

            stubAlreadyNotified(101, true);
            stubAlreadyNotified(102, false);

            var due = repo.findDue();

            assertEquals(1, due.size());
            var notification = due.get(0);
            assertThat(notification.recipients(), hasSize(1));
            assertEquals(555102L, notification.recipients().get(0).chatId());

            verify(notificationClient, never()).create(anyInt(), any());
            verify(guestClient, never()).getByRowId(GUESTS_TABLE, 103);
        }
    }

    @Nested
    @DisplayName("outside working hours")
    class OutsideWorkingHours {

        @Test
        @DisplayName("returns empty immediately without touching any Baserow client")
        void skipsEntirelyOutsideWorkingHours() {
            pinNow(EVENT_START.minusDays(1).withHour(22).withMinute(0)); // 22:00, outside 9-21

            var due = repo.findDue();

            assertThat(due, empty());
            verifyNoInteractions(eventClient, registrationClient, guestClient, notificationClient, resultClient);
        }

        @Test
        @DisplayName("boundary: hour 9 is included, hour 21 is excluded")
        void workingHourBoundaries() {
            stubEvents(event());
            stubNoExistingAnchor();
            stubRegistrations(registration(1, 101, EVENT_START.minusDays(4)));
            stubGuest(101, 555101L);
            stubAlreadyNotified(101, false);

            pinNow(EVENT_START.minusDays(1).withHour(21).withMinute(0)); // 21:00 sharp -> excluded
            assertThat(repo.findDue(), empty());

            // 09:00 the day of the event: still inside wave A's 24h overdue window (wave A was
            // 2026-07-24T14:00, window runs until 2026-07-25T14:00), and inside working hours.
            pinNow(EVENT_START.withHour(9).withMinute(0));
            assertThat(repo.findDue(), hasSize(1));
        }
    }

    @Nested
    @DisplayName("event-level guards")
    class EventLevelGuards {

        @Test
        @DisplayName("event with unparseable/blank date_start is skipped, doesn't blow up findDue()")
        void skipsEventWithUnparseableDate() {
            pinNow(EVENT_START.minusDays(1).withHour(15).withMinute(0));

            var badEvent = new BaserowEventRow(
                EVENT_ROW_ID, UUID.randomUUID(), "Broken Event",
                "not-a-date", null, "desc", null, URI.create("https://example.com"),
                null, null, true, List.of(), null, 1, true, true, false, null, List.of(), null
            );
            stubEvents(badEvent);

            var due = repo.findDue();

            assertThat(due, empty());
            verifyNoInteractions(registrationClient, guestClient, notificationClient, resultClient);
        }

        @Test
        @DisplayName("event that has already started is skipped even if a guest never got notified")
        void skipsEventAlreadyStarted() {
            pinNow(EVENT_START.plusHours(1)); // event already started an hour ago

            stubEvents(event());

            var due = repo.findDue();

            assertThat(due, empty());
            verifyNoInteractions(registrationClient, guestClient, notificationClient, resultClient);
        }

        @Test
        @DisplayName("two events in the same poll are evaluated independently")
        void multipleEventsHandledIndependently() {
            pinNow(EVENT_START.minusDays(1).withHour(15).withMinute(0)); // wave A due for EVENT_START only

            int otherEventRowId = 9999;
            var farFutureEvent = new BaserowEventRow(
                otherEventRowId, UUID.randomUUID(), "Far Future Event",
                iso(EVENT_START.plusDays(30)), null, "desc", null, URI.create("https://example.com"),
                null, null, true, List.of(), null, 1, true, true, false, null, List.of(), null
            );
            stubEvents(event(), farFutureEvent);
            stubNoExistingAnchor();
            stubRegistrations(registration(1, 101, EVENT_START.minusDays(4)));
            stubGuest(101, 555101L);
            stubAlreadyNotified(101, false);
            // No registrations stub for otherEventRowId — if the far-future event were queried at
            // all, Mockito's default null answer would NPE, so this also proves it's never reached.

            var due = repo.findDue();

            assertThat(due, hasSize(1));
            assertEquals("LAN Party", due.get(0).eventName());
            assertThat(due.get(0).recipients(), hasSize(1));
            verify(registrationClient, never()).findByEventRowIdRaw(REGISTRATIONS_TABLE, otherEventRowId);
        }
    }

    @Nested
    @DisplayName("registration-level guards")
    class RegistrationLevelGuards {

        @Test
        @DisplayName("registration with no linked guest is skipped")
        void skipsRegistrationWithoutGuest() {
            pinNow(EVENT_START.minusDays(1).withHour(15).withMinute(0));

            stubEvents(event());
            var noGuest = new BaserowRegistrationRow(
                1, UUID.randomUUID(), List.of(new BaserowLinkToTable(EVENT_ROW_ID, "e")),
                List.of(), iso(EVENT_START.minusDays(4)), 1, "", null, false
            );
            stubRegistrations(noGuest);

            var due = repo.findDue();

            assertThat(due, empty());
            verifyNoInteractions(guestClient, notificationClient);
        }

        @Test
        @DisplayName("registration with missing/unparseable created_at fails open (treated as registered at epoch -> eligible for the earliest due wave)")
        void missingCreatedAtFailsOpen() {
            pinNow(EVENT_START.minusDays(1).withHour(15).withMinute(0));

            stubEvents(event());
            stubNoExistingAnchor();
            var noCreatedAt = new BaserowRegistrationRow(
                1, UUID.randomUUID(), List.of(new BaserowLinkToTable(EVENT_ROW_ID, "e")),
                List.of(new BaserowLinkToTable(101, "g")), null, 1, "", null, false
            );
            stubRegistrations(noCreatedAt);
            stubGuest(101, 555101L);
            stubAlreadyNotified(101, false);

            var due = repo.findDue();

            assertThat(due, hasSize(1));
            assertThat(due.get(0).recipients(), hasSize(1));
        }

        @Test
        @DisplayName("two registration rows for the same guest on the same event only notify once")
        void duplicateRegistrationsForSameGuestNotifyOnce() {
            pinNow(EVENT_START.minusDays(1).withHour(15).withMinute(0));

            stubEvents(event());
            stubNoExistingAnchor();
            stubRegistrations(
                registration(1, 101, EVENT_START.minusDays(4)),
                registration(2, 101, EVENT_START.minusDays(3)) // same guestRowId, different registration row
            );
            stubGuest(101, 555101L);
            stubAlreadyNotified(101, false);

            var due = repo.findDue();

            assertThat(due, hasSize(1));
            assertThat(due.get(0).recipients(), hasSize(1));
            verify(guestClient, times(1)).getByRowId(GUESTS_TABLE, 101);
        }

        @Test
        @DisplayName("guest registered after wave B never gets an automated reminder, even once wave B is long overdue")
        void tooLateRegistrantNeverNotifiedEvenWhenOverdue() {
            // 2h after wave B (still well before the event itself starts at 19:00, and within
            // wave B's 24h overdue window, so wave B itself is still "due"); the too-late guest
            // still shouldn't qualify, because they registered after wave B's cutoff in the first place.
            pinNow(EVENT_START.withHour(10).withMinute(15).plusHours(2));

            stubEvents(event());
            stubExistingAnchor();
            stubRegistrations(registration(1, 103, EVENT_START.withHour(11))); // registered after wave B

            var due = repo.findDue();

            assertThat(due, empty());
            verifyNoInteractions(guestClient);
        }

        @Test
        @DisplayName("guest assigned to wave A is not retroactively bumped to wave B once wave A is more than 24h overdue")
        void missedWaveAIsNotRetried() {
            // 25h after wave A (past the 24h overdue window) but still well before wave B.
            pinNow(EVENT_START.minusDays(1).withHour(14).plusHours(25));

            stubEvents(event());
            stubExistingAnchor();
            stubRegistrations(registration(1, 101, EVENT_START.minusDays(4))); // assigned to wave A only

            var due = repo.findDue();

            assertThat(due, empty());
            verifyNoInteractions(guestClient);
        }
    }
}
