package com.lan.app.infrastructure.baserow.repository;

import com.baserow.dto.BaserowLinkToTable;
import com.baserow.dto.BaserowListResponse;
import com.baserow.dto.BaserowSingleSelect;
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
import com.lan.app.infrastructure.baserow.dto.UpdateSurveySentRequest;
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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Covers the post-event feedback survey: sent one day after the event, 14:00 Yerevan, only to
 * guests who confirmed attendance (action=CONFIRMED) via the reminder flow, and only once.
 */
@QuarkusTest
@DisplayName("BaserowEventNotificationRepository — findSurveyDue()")
class BaserowEventNotificationRepositoryFindSurveyDueTest {

    static final int EVENTS_TABLE = 992074;
    static final int REGISTRATIONS_TABLE = 992071;
    static final int GUESTS_TABLE = 824729;
    static final int EVENT_NOTIFICATIONS_TABLE = 831434;
    static final int RESULTS_TABLE = 1042307;
    static final ZoneId YEREVAN = ZoneId.of("Asia/Yerevan");

    static final int EVENT_ROW_ID = 4242;
    static final int ANCHOR_ROW_ID = 900;
    static final int GUEST_ROW_ID = 101;
    static final Long CHAT_ID = 555101L;

    // Event happens 2026-07-25 at 19:00 Yerevan -> survey due 2026-07-26T14:00 Yerevan.
    static final ZonedDateTime EVENT_START = ZonedDateTime.of(2026, 7, 25, 19, 0, 0, 0, YEREVAN);
    static final ZonedDateTime SURVEY_TIME = ZonedDateTime.of(2026, 7, 26, 14, 0, 0, 0, YEREVAN);

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
            null, null, true, List.of(), null, 1, true, true, false, null, List.of(), null, null
        );
    }

    private static BaserowRegistrationRow registration(int id, int guestRowId, boolean cancelled) {
        return new BaserowRegistrationRow(
            id, UUID.randomUUID(),
            List.of(new BaserowLinkToTable(EVENT_ROW_ID, "e")),
            List.of(new BaserowLinkToTable(guestRowId, "g")),
            iso(EVENT_START.minusDays(4)), 1, "", null, false, cancelled
        );
    }

    private static BaserowGuestRow guest(int rowId, Long chatId) {
        return new BaserowGuestRow(rowId, UUID.randomUUID(), "Guest", String.valueOf(rowId), "+374", null, chatId, null, null, null, null, null, null);
    }

    private static <T> BaserowListResponse<T> listOf(T... items) {
        return new BaserowListResponse<>(items.length, null, null, List.of(items));
    }

    private void stubEvents(BaserowEventRow... events) {
        when(eventClient.listAll(EVENTS_TABLE)).thenReturn(listOf(events));
    }

    private void stubRegistrations(BaserowRegistrationRow... regs) {
        when(registrationClient.findByEventRowIdRaw(REGISTRATIONS_TABLE, EVENT_ROW_ID)).thenReturn(listOf(regs));
    }

    private void stubGuest(int guestRowId, Long chatId) {
        when(guestClient.getByRowId(GUESTS_TABLE, guestRowId)).thenReturn(guest(guestRowId, chatId));
    }

    private void stubExistingAnchor() {
        when(notificationClient.findByEventIdRaw(EVENT_NOTIFICATIONS_TABLE, EVENT_ROW_ID)).thenReturn(
            listOf(new BaserowEventNotificationRow(ANCHOR_ROW_ID, UUID.randomUUID(), null, List.of(),
                List.of(new BaserowLinkToTable(EVENT_ROW_ID, "e")), true))
        );
    }

    private void stubNoAnchor() {
        when(notificationClient.findByEventIdRaw(EVENT_NOTIFICATIONS_TABLE, EVENT_ROW_ID)).thenReturn(listOf());
    }

    private void stubResultRow(int guestRowId, String action, Boolean surveySent) {
        when(resultClient.findByNotificationAndGuestRaw(RESULTS_TABLE, ANCHOR_ROW_ID, guestRowId)).thenReturn(
            listOf(new BaserowEventNotificationResultRow(
                1, List.of(), List.of(),
                null,
                action == null ? null : new BaserowSingleSelect(1, action, "green"),
                null, null, surveySent
            ))
        );
    }

    private void stubNoResultRow(int guestRowId) {
        when(resultClient.findByNotificationAndGuestRaw(RESULTS_TABLE, ANCHOR_ROW_ID, guestRowId)).thenReturn(listOf());
    }

    @Nested
    @DisplayName("eligible recipient")
    class EligibleRecipient {

        @Test
        @DisplayName("confirmed, not yet surveyed, due time reached -> included and survey_sent patched to true")
        void confirmedGuest_isIncludedAndMarkedSurveyed() {
            pinNow(SURVEY_TIME.plusMinutes(1));

            stubEvents(event());
            stubExistingAnchor();
            stubRegistrations(registration(1, GUEST_ROW_ID, false));
            stubGuest(GUEST_ROW_ID, CHAT_ID);
            stubResultRow(GUEST_ROW_ID, "CONFIRMED", null);

            var due = repo.findSurveyDue();

            assertThat(due, hasSize(1));
            var recipient = due.get(0);
            assertEquals(EVENT_ROW_ID, recipient.eventRowId());
            assertEquals("LAN Party", recipient.eventName());
            assertEquals(GUEST_ROW_ID, recipient.guestRowId());
            assertEquals(1, recipient.registrationRowId());
            assertEquals(CHAT_ID, recipient.chatId());

            verify(resultClient).updateSurveySent(RESULTS_TABLE, 1, new UpdateSurveySentRequest(true));
        }
    }

    @Nested
    @DisplayName("exclusions")
    class Exclusions {

        @Test
        @DisplayName("guest declined the reminder -> excluded")
        void declinedGuest_isExcluded() {
            pinNow(SURVEY_TIME.plusMinutes(1));

            stubEvents(event());
            stubExistingAnchor();
            stubRegistrations(registration(1, GUEST_ROW_ID, false));
            stubResultRow(GUEST_ROW_ID, "DECLINED", null);

            var due = repo.findSurveyDue();

            assertThat(due, empty());
            verify(resultClient, never()).updateSurveySent(anyInt(), anyInt(), any());
        }

        @Test
        @DisplayName("no result row at all (never answered the reminder) -> excluded")
        void noResultRow_isExcluded() {
            pinNow(SURVEY_TIME.plusMinutes(1));

            stubEvents(event());
            stubExistingAnchor();
            stubRegistrations(registration(1, GUEST_ROW_ID, false));
            stubNoResultRow(GUEST_ROW_ID);

            var due = repo.findSurveyDue();

            assertThat(due, empty());
        }

        @Test
        @DisplayName("survey already sent -> excluded, not sent twice")
        void alreadySurveyed_isExcluded() {
            pinNow(SURVEY_TIME.plusMinutes(1));

            stubEvents(event());
            stubExistingAnchor();
            stubRegistrations(registration(1, GUEST_ROW_ID, false));
            stubResultRow(GUEST_ROW_ID, "CONFIRMED", true);

            var due = repo.findSurveyDue();

            assertThat(due, empty());
            verify(resultClient, never()).updateSurveySent(anyInt(), anyInt(), any());
        }

        @Test
        @DisplayName("cancelled registration -> excluded")
        void cancelledRegistration_isExcluded() {
            pinNow(SURVEY_TIME.plusMinutes(1));

            stubEvents(event());
            stubExistingAnchor();
            stubRegistrations(registration(1, GUEST_ROW_ID, true));

            var due = repo.findSurveyDue();

            assertThat(due, empty());
            verifyNoInteractions(resultClient);
        }

        @Test
        @DisplayName("no anchor row for the event (reminders never sent) -> whole event skipped")
        void noAnchorRow_skipsEventEntirely() {
            pinNow(SURVEY_TIME.plusMinutes(1));

            stubEvents(event());
            stubNoAnchor();

            var due = repo.findSurveyDue();

            assertThat(due, empty());
            verifyNoInteractions(registrationClient, resultClient);
        }

        @Test
        @DisplayName("guest has no telegram chat id -> excluded")
        void noChatId_isExcluded() {
            pinNow(SURVEY_TIME.plusMinutes(1));

            stubEvents(event());
            stubExistingAnchor();
            stubRegistrations(registration(1, GUEST_ROW_ID, false));
            stubGuest(GUEST_ROW_ID, null);
            stubResultRow(GUEST_ROW_ID, "CONFIRMED", null);

            var due = repo.findSurveyDue();

            assertThat(due, empty());
            verify(resultClient, never()).updateSurveySent(anyInt(), anyInt(), any());
        }
    }

    @Nested
    @DisplayName("timing")
    class Timing {

        @Test
        @DisplayName("not yet due (before 14:00 the day after) -> excluded")
        void notYetDue_isExcluded() {
            pinNow(SURVEY_TIME.minusHours(1));

            stubEvents(event());

            var due = repo.findSurveyDue();

            assertThat(due, empty());
            verifyNoInteractions(registrationClient, guestClient, resultClient);
        }

        @Test
        @DisplayName("more than 24h overdue -> excluded")
        void tooOverdue_isExcluded() {
            pinNow(SURVEY_TIME.plusHours(25));

            stubEvents(event());

            var due = repo.findSurveyDue();

            assertThat(due, empty());
            verifyNoInteractions(registrationClient, guestClient, resultClient);
        }

        @Test
        @DisplayName("outside working hours -> excluded even if otherwise due")
        void outsideWorkingHours_isExcluded() {
            pinNow(SURVEY_TIME.withHour(22)); // 22:00, outside 9-21

            var due = repo.findSurveyDue();

            assertThat(due, empty());
            verifyNoInteractions(eventClient, registrationClient, guestClient, notificationClient, resultClient);
        }
    }
}
