package com.lan.app.api.resource;

import com.lan.app.domain.model.EventCapacityAlert;
import com.lan.app.domain.model.EventNotificationDue;
import com.lan.app.domain.model.EventRegistrationItem;
import com.lan.app.domain.model.NotificationRecipient;
import com.lan.app.service.EventCapacityAlertService;
import com.lan.app.service.EventNotificationService;
import com.lan.app.service.EventRegistrationService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class BotResourceTest {

    static final String BASE_PATH = "/events/v1/bot";

    @InjectMock
    EventRegistrationService service;

    @InjectMock
    EventNotificationService notificationService;

    @InjectMock
    EventCapacityAlertService capacityAlertService;

    @Nested
    @DisplayName("GET /events/v1/bot/my-registrations")
    class MyRegistrations {

        @Test
        @DisplayName("без chatId → 400")
        void missingChatId_returns400() {
            given()
                .when().get(BASE_PATH + "/my-registrations")
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("есть регистрации → 200 массив")
        void withRegistrations_returnsList() {
            Instant date = Instant.parse("2026-05-10T18:00:00Z");
            when(service.findByChatId(123L)).thenReturn(List.of(new EventRegistrationItem("Событие", date)));

            given()
                .queryParam("chatId", 123L)
                .when().get(BASE_PATH + "/my-registrations")
                .then()
                .statusCode(200)
                .body("$",               hasSize(1))
                .body("[0].event_name",  equalTo("Событие"));
        }

        @Test
        @DisplayName("нет регистраций → 200 пустой массив")
        void noRegistrations_returnsEmptyList() {
            when(service.findByChatId(123L)).thenReturn(List.of());

            given()
                .queryParam("chatId", 123L)
                .when().get(BASE_PATH + "/my-registrations")
                .then()
                .statusCode(200)
                .body("$", hasSize(0));
        }
    }

    @Nested
    @DisplayName("GET /events/v1/bot/event-notifications/due")
    class DueEventNotifications {

        @Test
        @DisplayName("есть уведомления с получателями → 200 с маппингом полей")
        void withNotifications_returnsMappedList() {
            var recipient = new NotificationRecipient(555L, 7, 9);
            var due = new EventNotificationDue(1, "EN", "RU", "Event", List.of(recipient));
            when(notificationService.findDue()).thenReturn(List.of(due));

            given()
                .when().get(BASE_PATH + "/event-notifications/due")
                .then()
                .statusCode(200)
                .body("$",                              hasSize(1))
                .body("[0].id",                          equalTo(1))
                .body("[0].messageEn",                   equalTo("EN"))
                .body("[0].recipients",                  hasSize(1))
                .body("[0].recipients[0].chatId",         equalTo(555))
                .body("[0].recipients[0].guestRowId",     equalTo(7))
                .body("[0].recipients[0].registrationRowId", equalTo(9));
        }

        @Test
        @DisplayName("нет уведомлений → 200 пустой массив")
        void noNotifications_returnsEmptyList() {
            when(notificationService.findDue()).thenReturn(List.of());

            given()
                .when().get(BASE_PATH + "/event-notifications/due")
                .then()
                .statusCode(200)
                .body("$", hasSize(0));
        }
    }

    @Nested
    @DisplayName("POST /events/v1/bot/event-notifications/{id}/mark-sent")
    class MarkSent {

        @Test
        @DisplayName("успешно → 200, сервис вызван")
        void success_returns200() {
            given()
                .when().post(BASE_PATH + "/event-notifications/42/mark-sent")
                .then()
                .statusCode(200);

            verify(notificationService).markSent(42);
        }
    }

    @Nested
    @DisplayName("POST /events/v1/bot/event-notifications/{id}/mark-failed")
    class MarkFailed {

        @Test
        @DisplayName("успешно → 200, сервис вызван")
        void success_returns200() {
            given()
                .when().post(BASE_PATH + "/event-notifications/42/mark-failed")
                .then()
                .statusCode(200);

            verify(notificationService).markFailed(42);
        }
    }

    @Nested
    @DisplayName("POST /events/v1/bot/event-notifications/{id}/results")
    class SaveResults {

        @Test
        @DisplayName("непустой список → 200, сервис вызван")
        void withResults_savesAndReturns200() {
            given()
                .contentType(ContentType.JSON)
                .body("""
                    [ { "guestRowId": 1, "registrationRowId": 2, "status": "SENT" } ]
                """)
                .when().post(BASE_PATH + "/event-notifications/42/results")
                .then()
                .statusCode(200);

            verify(notificationService).saveResults(org.mockito.ArgumentMatchers.eq(42), org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("пустой список → 200, сервис не вызывается")
        void emptyResults_skipsService() {
            given()
                .contentType(ContentType.JSON)
                .body("[]")
                .when().post(BASE_PATH + "/event-notifications/42/results")
                .then()
                .statusCode(200);

            org.mockito.Mockito.verifyNoInteractions(notificationService);
        }
    }

    @Nested
    @DisplayName("POST /events/v1/bot/event-notifications/{id}/action")
    class RecordAction {

        @Test
        @DisplayName("валидный action → 200, сервис вызван")
        void validAction_returns200() {
            given()
                .contentType(ContentType.JSON)
                .body("""
                    { "guestRowId": 1, "registrationRowId": 2, "action": "CONFIRMED" }
                """)
                .when().post(BASE_PATH + "/event-notifications/42/action")
                .then()
                .statusCode(200);

            verify(notificationService).recordGuestAction(42, 1, 2, "CONFIRMED");
        }

        @Test
        @DisplayName("отсутствует action → 400")
        void missingAction_returns400() {
            given()
                .contentType(ContentType.JSON)
                .body("""
                    { "guestRowId": 1, "registrationRowId": 2 }
                """)
                .when().post(BASE_PATH + "/event-notifications/42/action")
                .then()
                .statusCode(400);

            org.mockito.Mockito.verifyNoInteractions(notificationService);
        }

        @Test
        @DisplayName("пустой action → 400")
        void blankAction_returns400() {
            given()
                .contentType(ContentType.JSON)
                .body("""
                    { "guestRowId": 1, "registrationRowId": 2, "action": "" }
                """)
                .when().post(BASE_PATH + "/event-notifications/42/action")
                .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("GET /events/v1/bot/event-capacity-alerts/due")
    class DueCapacityAlerts {

        @Test
        @DisplayName("есть освободившиеся события → 200 массив")
        void withAlerts_returnsList() {
            when(capacityAlertService.findDue()).thenReturn(List.of(new EventCapacityAlert("Событие", 5, 10)));

            given()
                .when().get(BASE_PATH + "/event-capacity-alerts/due")
                .then()
                .statusCode(200)
                .body("$",                  hasSize(1))
                .body("[0].eventName",      equalTo("Событие"))
                .body("[0].registeredCount", equalTo(5))
                .body("[0].maxCapacity",     equalTo(10));
        }

        @Test
        @DisplayName("нет освободившихся событий → 200 пустой массив")
        void noAlerts_returnsEmptyList() {
            when(capacityAlertService.findDue()).thenReturn(List.of());

            given()
                .when().get(BASE_PATH + "/event-capacity-alerts/due")
                .then()
                .statusCode(200)
                .body("$", hasSize(0));
        }
    }
}
