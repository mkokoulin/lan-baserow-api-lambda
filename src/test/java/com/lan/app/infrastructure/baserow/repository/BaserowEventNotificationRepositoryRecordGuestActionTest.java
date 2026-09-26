package com.lan.app.infrastructure.baserow.repository;

import com.baserow.dto.BaserowListResponse;
import com.lan.app.infrastructure.baserow.client.BaserowEventClient;
import com.lan.app.infrastructure.baserow.client.BaserowEventNotificationClient;
import com.lan.app.infrastructure.baserow.client.BaserowEventNotificationResultClient;
import com.lan.app.infrastructure.baserow.client.BaserowEventRegistrationClient;
import com.lan.app.infrastructure.baserow.client.BaserowGuestClient;
import com.lan.app.infrastructure.baserow.dto.BaserowEventNotificationResultRow;
import com.lan.app.infrastructure.baserow.dto.UpdateNotificationResultActionRequest;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers recordGuestAction(), invoked when a guest taps "Всё в силе" / "Не смогу" on a reminder.
 * This is a Telegram callback handler path: it must never throw back into the bot (a guest's tap
 * failing to persist shouldn't crash the callback handler), it must tolerate the result row not
 * existing yet (older/edge-case sends), and if duplicate result rows exist for the same guest it
 * must update all of them rather than silently acting on just one.
 */
@QuarkusTest
@DisplayName("BaserowEventNotificationRepository — recordGuestAction()")
class BaserowEventNotificationRepositoryRecordGuestActionTest {

    static final int RESULTS_TABLE = 1042307;
    static final int NOTIFICATION_ROW_ID = 900;
    static final int GUEST_ROW_ID = 101;
    static final int REGISTRATION_ROW_ID = 1;

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

    private static BaserowEventNotificationResultRow row(int id) {
        return new BaserowEventNotificationResultRow(id, List.of(), List.of(), null, null, null, null, null);
    }

    private static <T> BaserowListResponse<T> listOf(T... items) {
        return new BaserowListResponse<>(items.length, null, null, List.of(items));
    }

    @Nested
    @DisplayName("existing result row")
    class ExistingResultRow {

        @Test
        @DisplayName("one existing row -> updates that row's action, never creates a new one")
        void updatesExistingRow_doesNotCreate() {
            when(resultClient.findByNotificationAndGuestRaw(RESULTS_TABLE, NOTIFICATION_ROW_ID, GUEST_ROW_ID))
                .thenReturn(listOf(row(55)));

            repo.recordGuestAction(NOTIFICATION_ROW_ID, GUEST_ROW_ID, REGISTRATION_ROW_ID, "CONFIRMED");

            verify(resultClient).updateAction(RESULTS_TABLE, 55, new UpdateNotificationResultActionRequest("CONFIRMED"));
            verify(resultClient, never()).create(anyInt(), any());
        }

        @Test
        @DisplayName("duplicate result rows for the same guest -> updates every one of them")
        void duplicateRows_updatesAll() {
            when(resultClient.findByNotificationAndGuestRaw(RESULTS_TABLE, NOTIFICATION_ROW_ID, GUEST_ROW_ID))
                .thenReturn(listOf(row(55), row(56)));

            repo.recordGuestAction(NOTIFICATION_ROW_ID, GUEST_ROW_ID, REGISTRATION_ROW_ID, "DECLINED");

            verify(resultClient).updateAction(RESULTS_TABLE, 55, new UpdateNotificationResultActionRequest("DECLINED"));
            verify(resultClient).updateAction(RESULTS_TABLE, 56, new UpdateNotificationResultActionRequest("DECLINED"));
        }
    }

    @Nested
    @DisplayName("missing result row")
    class MissingResultRow {

        @Test
        @DisplayName("no existing row -> creates one linking the notification/guest/registration, then updates its action")
        void noExistingRow_createsThenUpdates() {
            when(resultClient.findByNotificationAndGuestRaw(RESULTS_TABLE, NOTIFICATION_ROW_ID, GUEST_ROW_ID))
                .thenReturn(listOf()) // first check: nothing yet
                .thenReturn(listOf(row(77))); // re-fetch after create: the new row

            repo.recordGuestAction(NOTIFICATION_ROW_ID, GUEST_ROW_ID, REGISTRATION_ROW_ID, "CONFIRMED");

            verify(resultClient).create(eq(RESULTS_TABLE), argThat(req ->
                req.eventNotification().equals(List.of(NOTIFICATION_ROW_ID))
                    && req.guest().equals(List.of(GUEST_ROW_ID))
                    && req.eventRegistrations().equals(List.of(REGISTRATION_ROW_ID))
                    && "SENT".equals(req.status())
            ));
            verify(resultClient).updateAction(RESULTS_TABLE, 77, new UpdateNotificationResultActionRequest("CONFIRMED"));
            verify(resultClient, times(2))
                .findByNotificationAndGuestRaw(RESULTS_TABLE, NOTIFICATION_ROW_ID, GUEST_ROW_ID);
        }

        @Test
        @DisplayName("no existing row and the re-fetch after create still comes back empty -> no update is attempted, nothing throws")
        void createSucceedsButRefetchStillEmpty_noUpdateAttempted() {
            when(resultClient.findByNotificationAndGuestRaw(RESULTS_TABLE, NOTIFICATION_ROW_ID, GUEST_ROW_ID))
                .thenReturn(listOf());

            repo.recordGuestAction(NOTIFICATION_ROW_ID, GUEST_ROW_ID, REGISTRATION_ROW_ID, "CONFIRMED");

            verify(resultClient).create(eq(RESULTS_TABLE), any());
            verify(resultClient, never()).updateAction(anyInt(), anyInt(), any());
        }
    }

    @Nested
    @DisplayName("failure isolation")
    class FailureIsolation {

        @Test
        @DisplayName("lookup throws -> swallowed, does not propagate to the Telegram callback handler")
        void lookupThrows_doesNotPropagate() {
            when(resultClient.findByNotificationAndGuestRaw(RESULTS_TABLE, NOTIFICATION_ROW_ID, GUEST_ROW_ID))
                .thenThrow(new RuntimeException("Baserow 500"));

            repo.recordGuestAction(NOTIFICATION_ROW_ID, GUEST_ROW_ID, REGISTRATION_ROW_ID, "CONFIRMED");

            verify(resultClient, never()).updateAction(anyInt(), anyInt(), any());
        }

        @Test
        @DisplayName("updateAction throws for the first of two duplicate rows -> the second row is never attempted, and nothing propagates")
        void updateActionThrowsOnFirstRow_stopsButDoesNotPropagate() {
            when(resultClient.findByNotificationAndGuestRaw(RESULTS_TABLE, NOTIFICATION_ROW_ID, GUEST_ROW_ID))
                .thenReturn(listOf(row(55), row(56)));
            doThrow(new RuntimeException("Baserow 500"))
                .when(resultClient).updateAction(eq(RESULTS_TABLE), eq(55), any());

            repo.recordGuestAction(NOTIFICATION_ROW_ID, GUEST_ROW_ID, REGISTRATION_ROW_ID, "CONFIRMED");

            verify(resultClient, never()).updateAction(eq(RESULTS_TABLE), eq(56), any());
        }
    }
}
