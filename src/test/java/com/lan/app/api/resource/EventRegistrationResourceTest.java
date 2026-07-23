package com.lan.app.api.resource;

import com.lan.app.domain.exception.BusinessConflictException;
import com.lan.app.domain.model.EventNotificationPreview;
import com.lan.app.domain.model.EventRegistration;
import com.lan.app.domain.model.EventRegistrationItem;
import com.lan.app.domain.model.Id;
import com.lan.app.service.EventNotificationService;
import com.lan.app.service.EventRegistrationService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class EventRegistrationResourceTest {

    static final String BASE_PATH = "/events/v1/registrations";

    @InjectMock
    EventRegistrationService service;

    @InjectMock
    EventNotificationService notificationService;

    @Inject
    RegistrationConfirmStore confirmStore;

    static final UUID EVENT_ID = UUID.fromString("66666666-7777-0000-0000-000000000001");
    static final UUID GUEST_ID = UUID.fromString("77777777-8888-0000-0000-000000000001");

    static EventRegistration registration(UUID regExternalId) {
        return new EventRegistration(
            new Id(10, regExternalId), new Id(1, EVENT_ID), new Id(2, GUEST_ID),
            2, "comment", "website", false
        );
    }

    @Nested
    @DisplayName("POST /events/v1/registrations")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class Create {

        @Test
        @DisplayName("валидный запрос → 201 с Location")
        void validRequest_returns201() {
            UUID regId = UUID.randomUUID();
            when(service.create(any())).thenReturn(registration(regId));

            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "eventId":    "%s",
                        "guestId":    "%s",
                        "guestCount": 2,
                        "source":     "website"
                    }
                """.formatted(EVENT_ID, GUEST_ID))
                .when().post(BASE_PATH)
                .then()
                .statusCode(201)
                .header("Location", containsString(regId.toString()))
                .body("id",         equalTo(regId.toString()))
                .body("guestCount", equalTo(2));
        }

        @Test
        @DisplayName("guestCount < 1 → 400")
        void invalidGuestCount_returns400() {
            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "eventId":    "%s",
                        "guestId":    "%s",
                        "guestCount": 0
                    }
                """.formatted(EVENT_ID, GUEST_ID))
                .when().post(BASE_PATH)
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("отсутствует eventId → 400")
        void missingEventId_returns400() {
            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "guestId":    "%s",
                        "guestCount": 1
                    }
                """.formatted(GUEST_ID))
                .when().post(BASE_PATH)
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("событие sold out → 409 BUSINESS_CONFLICT")
        void soldOut_returns409() {
            when(service.create(any())).thenThrow(
                new BusinessConflictException("Event is sold out.", Map.of("eventId", EVENT_ID.toString())));

            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "eventId":    "%s",
                        "guestId":    "%s",
                        "guestCount": 1
                    }
                """.formatted(EVENT_ID, GUEST_ID))
                .when().post(BASE_PATH)
                .then()
                .statusCode(409)
                .body("code", equalTo("BUSINESS_CONFLICT"));
        }
    }

    @Nested
    @DisplayName("POST /events/v1/registrations — без авторизации")
    class CreateUnauthenticated {

        @Test
        @DisplayName("без JWT → 401")
        void unauthenticated_returns401() {
            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "eventId":    "%s",
                        "guestId":    "%s",
                        "guestCount": 1
                    }
                """.formatted(EVENT_ID, GUEST_ID))
                .when().post(BASE_PATH)
                .then()
                .statusCode(401);
        }
    }

    @Nested
    @DisplayName("POST /{regId}/confirm")
    class Confirm {

        @Test
        @DisplayName("без chatId → 200, event_name из findByExternalId")
        void withoutChatId_returnsEventName() {
            UUID regId = UUID.randomUUID();
            when(service.findByExternalId(regId))
                .thenReturn(Optional.of(new EventRegistrationItem("Мастер-класс", Instant.now())));

            given()
                .contentType(ContentType.JSON)
                .when().post(BASE_PATH + "/" + regId + "/confirm")
                .then()
                .statusCode(200)
                .body("event_name", equalTo("Мастер-класс"));
        }

        @Test
        @DisplayName("некорректный regId (не UUID) → 200, event_name null, без исключений")
        void nonUuidRegId_returnsNullEventName() {
            given()
                .contentType(ContentType.JSON)
                .when().post(BASE_PATH + "/not-a-uuid/confirm")
                .then()
                .statusCode(200)
                .body("event_name", nullValue());
        }

        @Test
        @DisplayName("с chatId, есть кэш guestRowId → storeTelegramChatIdForGuest, Baserow-lookup не выполняется")
        void withChatId_cacheHit_storesForGuest() {
            String regId = UUID.randomUUID().toString();
            confirmStore.storeGuestRowId(regId, 42);

            given()
                .contentType(ContentType.JSON)
                .queryParam("chatId", 12345)
                .when().post(BASE_PATH + "/" + regId + "/confirm")
                .then()
                .statusCode(200);

            verify(service).storeTelegramChatIdForGuest(42, 12345L);
            verify(service, org.mockito.Mockito.never()).storeTelegramChatId(any(UUID.class), any());
        }

        @Test
        @DisplayName("с chatId, нет кэша, regId — валидный UUID → storeTelegramChatId по UUID")
        void withChatId_noCache_storesByUuid() {
            UUID regId = UUID.randomUUID();
            when(service.findByExternalId(regId)).thenReturn(Optional.empty());

            given()
                .contentType(ContentType.JSON)
                .queryParam("chatId", 12345)
                .when().post(BASE_PATH + "/" + regId + "/confirm")
                .then()
                .statusCode(200);

            verify(service).storeTelegramChatId(regId, 12345L);
        }

        @Test
        @DisplayName("с chatId, нет кэша, regId не UUID → chatId не сохраняется, без исключений")
        void withChatId_nonUuidRegId_ignoresChatId() {
            given()
                .contentType(ContentType.JSON)
                .queryParam("chatId", 12345)
                .when().post(BASE_PATH + "/not-a-uuid/confirm")
                .then()
                .statusCode(200);
        }
    }

    @Nested
    @DisplayName("GET /{regId}/confirmed")
    class IsConfirmed {

        @Test
        @DisplayName("не подтверждено → confirmed=false")
        void notConfirmed_returnsFalse() {
            String regId = UUID.randomUUID().toString();

            given()
                .when().get(BASE_PATH + "/" + regId + "/confirmed")
                .then()
                .statusCode(200)
                .body("confirmed", equalTo(false));
        }

        @Test
        @DisplayName("после confirm → confirmed=true")
        void afterConfirm_returnsTrue() {
            String regId = UUID.randomUUID().toString();

            given().contentType(ContentType.JSON).when().post(BASE_PATH + "/" + regId + "/confirm");

            given()
                .when().get(BASE_PATH + "/" + regId + "/confirmed")
                .then()
                .statusCode(200)
                .body("confirmed", equalTo(true));
        }
    }

    @Nested
    @DisplayName("POST /{regId}/mark-paid")
    class MarkPaid {

        @Test
        @DisplayName("некорректный regId → 400")
        void invalidRegId_returns400() {
            given()
                .contentType(ContentType.JSON)
                .when().post(BASE_PATH + "/not-a-uuid/mark-paid")
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("валидный regId → 200 с chatId")
        void validRegId_returns200() {
            UUID regId = UUID.randomUUID();
            when(service.markPaid(regId)).thenReturn(Optional.of(999L));

            given()
                .contentType(ContentType.JSON)
                .when().post(BASE_PATH + "/" + regId + "/mark-paid")
                .then()
                .statusCode(200)
                .body("chatId", equalTo(999));
        }

        @Test
        @DisplayName("chatId неизвестен → 200 chatId=null")
        void noChatId_returnsNull() {
            UUID regId = UUID.randomUUID();
            when(service.markPaid(regId)).thenReturn(Optional.empty());

            given()
                .contentType(ContentType.JSON)
                .when().post(BASE_PATH + "/" + regId + "/mark-paid")
                .then()
                .statusCode(200)
                .body("chatId", nullValue());
        }
    }

    @Nested
    @DisplayName("GET /{regId}/paid")
    class IsPaid {

        @Test
        @DisplayName("не оплачено → paid=false")
        void notPaid_returnsFalse() {
            UUID regId = UUID.randomUUID();
            when(service.markPaid(any())).thenReturn(Optional.empty());

            given()
                .when().get(BASE_PATH + "/" + regId + "/paid")
                .then()
                .statusCode(200)
                .body("paid", equalTo(false));
        }

        @Test
        @DisplayName("после mark-paid → paid=true")
        void afterMarkPaid_returnsTrue() {
            UUID regId = UUID.randomUUID();
            when(service.markPaid(regId)).thenReturn(Optional.empty());

            given().contentType(ContentType.JSON).when().post(BASE_PATH + "/" + regId + "/mark-paid");

            given()
                .when().get(BASE_PATH + "/" + regId + "/paid")
                .then()
                .statusCode(200)
                .body("paid", equalTo(true));
        }
    }

    @Nested
    @DisplayName("GET /{regId}/notifications/due")
    class DueNotifications {

        @Test
        @DisplayName("некорректный regId → 400")
        void invalidRegId_returns400() {
            given()
                .when().get(BASE_PATH + "/not-a-uuid/notifications/due")
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("событие не найдено по regId → 200 пустой массив")
        void eventNotFound_returnsEmptyList() {
            UUID regId = UUID.randomUUID();
            when(service.getEventRowIdByExternalId(regId)).thenReturn(Optional.empty());

            given()
                .when().get(BASE_PATH + "/" + regId + "/notifications/due")
                .then()
                .statusCode(200)
                .body("$", hasSize(0));
        }

        @Test
        @DisplayName("есть уведомления → 200 массив с маппингом полей")
        void withNotifications_returnsMappedList() {
            UUID regId = UUID.randomUUID();
            when(service.getEventRowIdByExternalId(regId)).thenReturn(Optional.of(5));
            when(notificationService.findDueForEvent(5))
                .thenReturn(List.of(new EventNotificationPreview(1, "EN msg", "RU msg", "Event")));

            given()
                .when().get(BASE_PATH + "/" + regId + "/notifications/due")
                .then()
                .statusCode(200)
                .body("$",             hasSize(1))
                .body("[0].id",        equalTo(1))
                .body("[0].messageEn", equalTo("EN msg"))
                .body("[0].messageRu", equalTo("RU msg"))
                .body("[0].eventName", equalTo("Event"));
        }
    }
}
