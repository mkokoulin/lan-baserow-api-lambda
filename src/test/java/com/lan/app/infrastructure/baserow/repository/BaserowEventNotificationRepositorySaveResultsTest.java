package com.lan.app.infrastructure.baserow.repository;

import com.lan.app.api.dto.request.NotificationResultRequest;
import com.lan.app.infrastructure.baserow.client.BaserowEventClient;
import com.lan.app.infrastructure.baserow.client.BaserowEventNotificationClient;
import com.lan.app.infrastructure.baserow.client.BaserowEventNotificationResultClient;
import com.lan.app.infrastructure.baserow.client.BaserowEventRegistrationClient;
import com.lan.app.infrastructure.baserow.client.BaserowGuestClient;
import com.lan.app.infrastructure.baserow.dto.UpdateEventNotificationStatusRequest;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Covers saveResults(), the write-back after the bot has actually attempted delivery over
 * Telegram. Since this is what stops a reminder from being re-sent on the next poll, a partial
 * write failure here (one guest's result row fails to persist) must not be silently swallowed —
 * it needs to still surface as the notification ending up FAILED, and it must not stop the other
 * guests' result rows from being written.
 */
@QuarkusTest
@DisplayName("BaserowEventNotificationRepository — saveResults()")
class BaserowEventNotificationRepositorySaveResultsTest {

    static final int EVENT_NOTIFICATIONS_TABLE = 831434;
    static final int RESULTS_TABLE = 1042307;
    static final int NOTIFICATION_ROW_ID = 900;

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

    private static NotificationResultRequest sent(int guestRowId, int regRowId) {
        return new NotificationResultRequest(guestRowId, regRowId, "SENT", null);
    }

    private static NotificationResultRequest failed(int guestRowId, int regRowId, String reason) {
        return new NotificationResultRequest(guestRowId, regRowId, "FAILED", reason);
    }

    @Nested
    @DisplayName("no-ops")
    class NoOps {

        @Test
        @DisplayName("null results -> no client calls at all")
        void nullResults_noop() {
            repo.saveResults(NOTIFICATION_ROW_ID, null);
            verifyNoInteractions(resultClient, notificationClient);
        }

        @Test
        @DisplayName("empty results -> no client calls at all")
        void emptyResults_noop() {
            repo.saveResults(NOTIFICATION_ROW_ID, List.of());
            verifyNoInteractions(resultClient, notificationClient);
        }
    }

    @Nested
    @DisplayName("status rollup")
    class StatusRollup {

        @Test
        @DisplayName("all delivered -> creates one row per result and marks the notification SENT")
        void allSent_marksNotificationSent() {
            repo.saveResults(NOTIFICATION_ROW_ID, List.of(sent(101, 1), sent(102, 2)));

            verify(resultClient, times(2)).create(eq(RESULTS_TABLE), any());
            verify(notificationClient).updateStatus(EVENT_NOTIFICATIONS_TABLE, NOTIFICATION_ROW_ID,
                new UpdateEventNotificationStatusRequest("SENT"));
        }

        @Test
        @DisplayName("one delivery failed among several -> notification marked FAILED even though the others succeeded")
        void oneFailed_marksNotificationFailed() {
            repo.saveResults(NOTIFICATION_ROW_ID, List.of(sent(101, 1), failed(102, 2, "blocked bot")));

            verify(resultClient, times(2)).create(eq(RESULTS_TABLE), any());
            verify(notificationClient).updateStatus(EVENT_NOTIFICATIONS_TABLE, NOTIFICATION_ROW_ID,
                new UpdateEventNotificationStatusRequest("FAILED"));
        }

        @Test
        @DisplayName("failed result row persists guestRowId, registrationRowId, status and failureReason")
        void failedResult_persistsFailureReason() {
            repo.saveResults(NOTIFICATION_ROW_ID, List.of(failed(102, 2, "blocked bot")));

            verify(resultClient).create(eq(RESULTS_TABLE), argThat(req ->
                req.guest().equals(List.of(102))
                    && req.eventRegistrations().equals(List.of(2))
                    && "FAILED".equals(req.status())
                    && "blocked bot".equals(req.failureReason())
                    && req.eventNotification().equals(List.of(NOTIFICATION_ROW_ID))
            ));
        }
    }

    @Nested
    @DisplayName("partial write failures")
    class PartialWriteFailures {

        @Test
        @DisplayName("one row's create() throws -> the other row is still persisted, and the notification ends up FAILED")
        void oneCreateThrows_othersStillPersistedAndStatusFailed() {
            doThrow(new RuntimeException("Baserow 500"))
                .when(resultClient).create(eq(RESULTS_TABLE), argThat(req -> req.guest().equals(List.of(101))));

            repo.saveResults(NOTIFICATION_ROW_ID, List.of(sent(101, 1), sent(102, 2)));

            verify(resultClient, times(2)).create(eq(RESULTS_TABLE), any());
            verify(notificationClient).updateStatus(EVENT_NOTIFICATIONS_TABLE, NOTIFICATION_ROW_ID,
                new UpdateEventNotificationStatusRequest("FAILED"));
        }

        @Test
        @DisplayName("every row's create() throws -> saveResults doesn't propagate, still marks the notification FAILED")
        void allCreatesThrow_doesNotPropagateAndStillMarksFailed() {
            doThrow(new RuntimeException("Baserow 500")).when(resultClient).create(eq(RESULTS_TABLE), any());

            repo.saveResults(NOTIFICATION_ROW_ID, List.of(sent(101, 1)));

            verify(notificationClient).updateStatus(EVENT_NOTIFICATIONS_TABLE, NOTIFICATION_ROW_ID,
                new UpdateEventNotificationStatusRequest("FAILED"));
        }
    }
}
