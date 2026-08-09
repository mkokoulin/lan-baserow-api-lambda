package com.lan.app.infrastructure.baserow.repository;

import com.baserow.dto.BaserowListResponse;
import com.lan.app.infrastructure.baserow.client.BaserowGuestClient;
import com.lan.app.infrastructure.baserow.dto.BaserowGuestRow;
import com.lan.app.infrastructure.baserow.dto.UpdateHeardAboutSourceRequest;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * "Now" is pinned via the package-private {@code setClock} test hook (same pattern as
 * BaserowEventNotificationRepositoryFindDueTest) so the 24h-delay + working-hours math is
 * deterministic. WEEKDAY (2026-08-10) is a Monday, WEEKEND (2026-08-15) is a Saturday.
 */
@QuarkusTest
@DisplayName("BaserowHeardAboutSourceRepository")
class BaserowHeardAboutSourceRepositoryTest {

    static final int GUESTS_TABLE = 824729;
    static final ZoneId YEREVAN = ZoneId.of("Asia/Yerevan");

    static final ZonedDateTime WEEKDAY_NOON = ZonedDateTime.of(2026, 8, 10, 12, 0, 0, 0, YEREVAN);
    static final ZonedDateTime WEEKEND_NOON = ZonedDateTime.of(2026, 8, 15, 12, 0, 0, 0, YEREVAN);

    @Inject
    BaserowHeardAboutSourceRepository repo;

    @InjectMock
    @RestClient
    BaserowGuestClient guestClient;

    private void pinNow(ZonedDateTime now) {
        repo.setClock(Clock.fixed(now.toInstant(), YEREVAN));
    }

    private static String iso(ZonedDateTime zdt) {
        return DateTimeFormatter.ISO_INSTANT.format(zdt.toInstant());
    }

    private static BaserowGuestRow guest(int rowId, Long chatId, ZonedDateTime createdAt, Boolean surveySent) {
        return new BaserowGuestRow(
            rowId, UUID.randomUUID(), "Guest", String.valueOf(rowId), "+374", null, chatId,
            null, null, createdAt == null ? null : iso(createdAt), null, null, surveySent
        );
    }

    private static <T> BaserowListResponse<T> listOf(T... items) {
        return new BaserowListResponse<>(items.length, null, null, List.of(items));
    }

    private void stubGuests(BaserowGuestRow... guests) {
        when(guestClient.listAllRaw(GUESTS_TABLE)).thenReturn(listOf(guests));
    }

    @Nested
    @DisplayName("findDue() eligibility")
    class Eligibility {

        @Test
        @DisplayName("guest created 24h+ ago, with chat id, not yet surveyed -> due, and gets marked surveyed immediately")
        void eligibleGuestIsDueAndMarkedSent() {
            pinNow(WEEKDAY_NOON);
            stubGuests(guest(101, 555101L, WEEKDAY_NOON.minusHours(25), null));

            var due = repo.findDue();

            assertThat(due, hasSize(1));
            assertEquals(101, due.get(0).guestRowId());
            assertEquals(555101L, due.get(0).chatId());
            verify(guestClient).patchHeardAboutSource(GUESTS_TABLE, 101, new UpdateHeardAboutSourceRequest(null, null, true));
        }

        @Test
        @DisplayName("guest created less than 24h ago is skipped")
        void tooRecentGuestSkipped() {
            pinNow(WEEKDAY_NOON);
            stubGuests(guest(101, 555101L, WEEKDAY_NOON.minusHours(23), null));

            assertThat(repo.findDue(), empty());
            verify(guestClient, never()).patchHeardAboutSource(eq(GUESTS_TABLE), eq(101), org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("already-surveyed guest is skipped")
        void alreadySurveyedSkipped() {
            pinNow(WEEKDAY_NOON);
            stubGuests(guest(101, 555101L, WEEKDAY_NOON.minusHours(25), true));

            assertThat(repo.findDue(), empty());
        }

        @Test
        @DisplayName("guest without a linked Telegram chat is skipped")
        void noChatIdSkipped() {
            pinNow(WEEKDAY_NOON);
            stubGuests(guest(101, null, WEEKDAY_NOON.minusHours(25), null));

            assertThat(repo.findDue(), empty());
        }

        @Test
        @DisplayName("guest with unparseable/missing created_at is skipped")
        void unparseableCreatedAtSkipped() {
            pinNow(WEEKDAY_NOON);
            stubGuests(guest(101, 555101L, null, null));

            assertThat(repo.findDue(), empty());
        }

        @Test
        @DisplayName("guest whose 24h mark is more than 24h overdue (48h+ since creation) is skipped")
        void veryOverdueGuestSkipped() {
            pinNow(WEEKDAY_NOON);
            stubGuests(guest(101, 555101L, WEEKDAY_NOON.minusHours(49), null));

            assertThat(repo.findDue(), empty());
        }

        @Test
        @DisplayName("multiple guests in the same poll are evaluated independently")
        void multipleGuestsHandledIndependently() {
            pinNow(WEEKDAY_NOON);
            stubGuests(
                guest(101, 555101L, WEEKDAY_NOON.minusHours(25), null),  // due
                guest(102, 555102L, WEEKDAY_NOON.minusHours(10), null),  // too recent
                guest(103, null, WEEKDAY_NOON.minusHours(30), null),     // no chat id
                guest(104, 555104L, WEEKDAY_NOON.minusHours(25), true)   // already surveyed
            );

            var due = repo.findDue();

            assertThat(due, hasSize(1));
            assertEquals(101, due.get(0).guestRowId());
        }
    }

    @Nested
    @DisplayName("working-hours gate")
    class WorkingHours {

        @Test
        @DisplayName("weekday before 10:00 is outside working hours -> empty, no Baserow calls at all")
        void weekdayBeforeOpening() {
            pinNow(WEEKDAY_NOON.withHour(9).withMinute(59));

            assertThat(repo.findDue(), empty());
            verifyNoInteractions(guestClient);
        }

        @Test
        @DisplayName("weekday boundary: 10:00 included, 22:00 excluded")
        void weekdayBoundaries() {
            stubGuests(guest(101, 555101L, WEEKDAY_NOON.minusHours(25), null));

            pinNow(WEEKDAY_NOON.withHour(9).withMinute(59));
            assertThat(repo.findDue(), empty());

            pinNow(WEEKDAY_NOON.withHour(21).withMinute(59));
            assertThat(repo.findDue(), hasSize(1));
        }

        @Test
        @DisplayName("weekday 22:00 is outside working hours")
        void weekdayAfterClosing() {
            stubGuests(guest(101, 555101L, WEEKDAY_NOON.minusHours(25), null));
            pinNow(WEEKDAY_NOON.withHour(22).withMinute(0));

            assertThat(repo.findDue(), empty());
        }

        @Test
        @DisplayName("weekend boundary: 10:00 included, 16:00 excluded")
        void weekendBoundaries() {
            stubGuests(guest(101, 555101L, WEEKEND_NOON.minusHours(25), null));

            pinNow(WEEKEND_NOON.withHour(9).withMinute(59));
            assertThat(repo.findDue(), empty());

            pinNow(WEEKEND_NOON.withHour(15).withMinute(59));
            assertThat(repo.findDue(), hasSize(1));
        }

        @Test
        @DisplayName("weekend 17:00 (past 16:00 close) is outside working hours, even though weekday hours would allow it")
        void weekendAfterClosing() {
            stubGuests(guest(101, 555101L, WEEKEND_NOON.minusHours(25), null));
            pinNow(WEEKEND_NOON.withHour(17).withMinute(0));

            assertThat(repo.findDue(), empty());
        }
    }

    @Nested
    @DisplayName("saveAnswer()")
    class SaveAnswer {

        @Test
        @DisplayName("patches source, comment, and re-affirms survey_sent=true")
        void patchesGuestRow() {
            repo.saveAnswer(101, "Instagram", "found it via a friend's story");

            verify(guestClient).patchHeardAboutSource(
                GUESTS_TABLE, 101, new UpdateHeardAboutSourceRequest("Instagram", "found it via a friend's story", true)
            );
        }

        @Test
        @DisplayName("comment may be null (guest skipped it)")
        void nullCommentAllowed() {
            repo.saveAnswer(101, "Google", null);

            verify(guestClient).patchHeardAboutSource(
                GUESTS_TABLE, 101, new UpdateHeardAboutSourceRequest("Google", null, true)
            );
        }
    }
}
