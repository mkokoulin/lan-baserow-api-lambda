package com.lan.app.api.resource;

import com.lan.app.api.exception.ErrorCode;
import com.lan.app.domain.exception.BusinessConflictException;
import com.lan.app.domain.exception.ResourceNotFoundException;
import com.lan.app.domain.model.CoworkingGuest;
import com.lan.app.domain.model.CoworkingGuestTariff;
import com.lan.app.domain.model.EventRegistrationItem;
import com.lan.app.domain.model.GuestTariffStatus;
import com.lan.app.service.CoworkingGuestService;
import com.lan.app.service.CoworkingGuestTariffService;
import com.lan.app.service.EventRegistrationService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@QuarkusTest
class CoworkingGuestResourceTest {

    static final String BASE_PATH = "/coworking/v1/guests";

    @InjectMock
    CoworkingGuestService service;

    @InjectMock
    CoworkingGuestTariffService tariffService;

    @InjectMock
    EventRegistrationService registrationService;

    // ─────────────────────────────────────────────────────────────────
    // Test fixtures
    // ─────────────────────────────────────────────────────────────────

    static final UUID   GUEST_ID = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");
    static final Long   CHAT_ID  = 123456789L;
    static final String PHONE    = "+79161234567";

    static CoworkingGuest guest() {
        return new CoworkingGuest(GUEST_ID, CHAT_ID, "Ivan", "Petrov", "ivan_petrov", PHONE);
    }

    static CoworkingGuest guestNoChatId() {
        return new CoworkingGuest(GUEST_ID, null, "Ivan", "Petrov", "ivan_petrov", PHONE);
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /coworking/v1/guests?chatId=…
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /guests?chatId=…")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class GetByChatId {

        @Test
        @DisplayName("без chatId → 400")
        void missingChatId_returns400() {
            given()
                .when().get(BASE_PATH)
                .then()
                .statusCode(400);

            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("гость найден → 200 со всеми полями")
        void found_returns200() {
            when(service.findByChatId(CHAT_ID)).thenReturn(Optional.of(guest()));

            given()
                .queryParam("chatId", CHAT_ID)
                .when().get(BASE_PATH)
                .then()
                .statusCode(200)
                .body("id",             equalTo(GUEST_ID.toString()))
                .body("telegramChatId", equalTo(CHAT_ID.intValue()))
                .body("firstName",      equalTo("Ivan"))
                .body("lastName",       equalTo("Petrov"))
                .body("telegram",       equalTo("ivan_petrov"))
                .body("phone",          equalTo(PHONE));
        }

        @Test
        @DisplayName("гость не найден → 404")
        void notFound_returns404() {
            when(service.findByChatId(CHAT_ID)).thenReturn(Optional.empty());

            given()
                .queryParam("chatId", CHAT_ID)
                .when().get(BASE_PATH)
                .then()
                .statusCode(404);
        }
    }

    @Nested
    @DisplayName("GET /guests?chatId=… — без авторизации")
    class GetByChatIdUnauthenticated {

        @Test
        @DisplayName("без JWT → 401")
        void unauthenticated_returns401() {
            given()
                .queryParam("chatId", CHAT_ID)
                .when().get(BASE_PATH)
                .then()
                .statusCode(401);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /coworking/v1/guests/{externalId}
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /guests/{externalId}")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class GetById {

        @Test
        @DisplayName("существующий гость → 200 со всеми полями")
        void existing_returns200() {
            when(service.get(GUEST_ID)).thenReturn(guest());

            given()
                .when().get(BASE_PATH + "/" + GUEST_ID)
                .then()
                .statusCode(200)
                .body("id",        equalTo(GUEST_ID.toString()))
                .body("firstName", equalTo("Ivan"))
                .body("phone",     equalTo(PHONE));
        }

        @Test
        @DisplayName("гость не найден → 404 RESOURCE_NOT_FOUND")
        void notFound_returns404() {
            when(service.get(GUEST_ID))
                .thenThrow(new ResourceNotFoundException("CoworkingGuest", GUEST_ID));

            given()
                .when().get(BASE_PATH + "/" + GUEST_ID)
                .then()
                .statusCode(404)
                .body("code",               equalTo("RESOURCE_NOT_FOUND"))
                .body("details.externalId", equalTo(GUEST_ID.toString()));
        }

        @Test
        @DisplayName("некорректный UUID → 404")
        void invalidUuid_returns404() {
            given()
                .when().get(BASE_PATH + "/not-a-uuid")
                .then()
                .statusCode(404);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // POST /coworking/v1/guests
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /guests")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class Create {

        @Test
        @DisplayName("валидный запрос → 201 с Location")
        void validRequest_returns201() {
            when(service.create("Ivan", "Petrov", PHONE, "ivan_petrov", null))
                .thenReturn(guestNoChatId());

            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "firstName": "Ivan",
                        "lastName":  "Petrov",
                        "telegram":  "ivan_petrov",
                        "phone":     "%s"
                    }
                """.formatted(PHONE))
                .when().post(BASE_PATH)
                .then()
                .statusCode(201)
                .header("Location", containsString(GUEST_ID.toString()))
                .body("id",        equalTo(GUEST_ID.toString()))
                .body("firstName", equalTo("Ivan"));
        }

        @Test
        @DisplayName("с telegram_chat_id → 201")
        void withChatId_returns201() {
            when(service.create("Ivan", "Petrov", PHONE, "ivan_petrov", CHAT_ID))
                .thenReturn(guest());

            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "firstName":       "Ivan",
                        "lastName":        "Petrov",
                        "telegram":        "ivan_petrov",
                        "phone":           "%s",
                        "telegram_chat_id": "%s"
                    }
                """.formatted(PHONE, CHAT_ID))
                .when().post(BASE_PATH)
                .then()
                .statusCode(201);
        }

        @Test
        @DisplayName("отсутствует firstName → 400")
        void missingFirstName_returns400() {
            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "lastName": "Petrov",
                        "telegram": "ivan_petrov",
                        "phone":    "%s"
                    }
                """.formatted(PHONE))
                .when().post(BASE_PATH)
                .then()
                .statusCode(400);

            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("отсутствует phone → 400")
        void missingPhone_returns400() {
            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "firstName": "Ivan",
                        "lastName":  "Petrov",
                        "telegram":  "ivan_petrov"
                    }
                """)
                .when().post(BASE_PATH)
                .then()
                .statusCode(400);

            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("конфликт по телефону → 409 BUSINESS_CONFLICT")
        void phoneConflict_returns409() {
            when(service.create(any(), any(), any(), any(), any()))
                .thenThrow(new BusinessConflictException(
                    "Guest with phone " + PHONE + " already exists.",
                    Map.of("phone", PHONE)
                ));

            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "firstName": "Ivan",
                        "lastName":  "Petrov",
                        "telegram":  "ivan_petrov",
                        "phone":     "%s"
                    }
                """.formatted(PHONE))
                .when().post(BASE_PATH)
                .then()
                .statusCode(409)
                .body("code", equalTo("BUSINESS_CONFLICT"));
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /coworking/v1/guests/by-phone?phone=…
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /guests/by-phone")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class GetByPhone {

        @Test
        @DisplayName("без phone → 400")
        void missingPhone_returns400() {
            given()
                .when().get(BASE_PATH + "/by-phone")
                .then()
                .statusCode(400);

            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("гость найден → 200")
        void found_returns200() {
            when(service.findByPhone(PHONE)).thenReturn(Optional.of(guest()));

            given()
                .queryParam("phone", PHONE)
                .when().get(BASE_PATH + "/by-phone")
                .then()
                .statusCode(200)
                .body("id",    equalTo(GUEST_ID.toString()))
                .body("phone", equalTo(PHONE));
        }

        @Test
        @DisplayName("гость не найден → 404")
        void notFound_returns404() {
            when(service.findByPhone(PHONE)).thenReturn(Optional.empty());

            given()
                .queryParam("phone", PHONE)
                .when().get(BASE_PATH + "/by-phone")
                .then()
                .statusCode(404);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // PATCH /coworking/v1/guests/{externalId}/link-chat
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("PATCH /guests/{externalId}/link-chat")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class LinkChatById {

        @Test
        @DisplayName("успешно → 200")
        void success_returns200() {
            when(service.findByChatId(CHAT_ID)).thenReturn(Optional.empty());
            when(service.linkChatIdById(GUEST_ID, CHAT_ID)).thenReturn(guest());

            given()
                .contentType(ContentType.JSON)
                .body("""
                    { "chatId": %d }
                """.formatted(CHAT_ID))
                .when().patch(BASE_PATH + "/" + GUEST_ID + "/link-chat")
                .then()
                .statusCode(200)
                .body("id", equalTo(GUEST_ID.toString()));
        }

        @Test
        @DisplayName("chatId принадлежит тому же гостю → 200")
        void sameGuest_returns200() {
            when(service.findByChatId(CHAT_ID)).thenReturn(Optional.of(guest()));
            when(service.linkChatIdById(GUEST_ID, CHAT_ID)).thenReturn(guest());

            given()
                .contentType(ContentType.JSON)
                .body("""
                    { "chatId": %d }
                """.formatted(CHAT_ID))
                .when().patch(BASE_PATH + "/" + GUEST_ID + "/link-chat")
                .then()
                .statusCode(200);
        }

        @Test
        @DisplayName("chatId занят другим гостем → 409 TELEGRAM_CHAT_ID_CONFLICT")
        void conflict_returns409() {
            UUID otherGuestId = UUID.fromString("ffffffff-0000-0000-0000-000000000099");
            CoworkingGuest otherGuest = new CoworkingGuest(otherGuestId, CHAT_ID, "Other", "Guest", "other", "+70000000000");
            when(service.findByChatId(CHAT_ID)).thenReturn(Optional.of(otherGuest));

            given()
                .contentType(ContentType.JSON)
                .body("""
                    { "chatId": %d }
                """.formatted(CHAT_ID))
                .when().patch(BASE_PATH + "/" + GUEST_ID + "/link-chat")
                .then()
                .statusCode(409)
                .body("code", equalTo(ErrorCode.TELEGRAM_CHAT_ID_CONFLICT.name()));
        }

        @Test
        @DisplayName("отсутствует chatId → 400")
        void missingChatId_returns400() {
            given()
                .contentType(ContentType.JSON)
                .body("{}")
                .when().patch(BASE_PATH + "/" + GUEST_ID + "/link-chat")
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("гость не найден → 404")
        void guestNotFound_returns404() {
            when(service.findByChatId(CHAT_ID)).thenReturn(Optional.empty());
            when(service.linkChatIdById(GUEST_ID, CHAT_ID))
                .thenThrow(new ResourceNotFoundException("CoworkingGuest", GUEST_ID));

            given()
                .contentType(ContentType.JSON)
                .body("""
                    { "chatId": %d }
                """.formatted(CHAT_ID))
                .when().patch(BASE_PATH + "/" + GUEST_ID + "/link-chat")
                .then()
                .statusCode(404)
                .body("code", equalTo("RESOURCE_NOT_FOUND"));
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // POST /coworking/v1/guests/link-chat
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /guests/link-chat")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class LinkChat {

        @Test
        @DisplayName("успешно → 200")
        void success_returns200() {
            when(service.linkChatIdByPhone(PHONE, CHAT_ID)).thenReturn(Optional.of(guest()));

            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "phone":  "%s",
                        "chatId": %d
                    }
                """.formatted(PHONE, CHAT_ID))
                .when().post(BASE_PATH + "/link-chat")
                .then()
                .statusCode(200)
                .body("id", equalTo(GUEST_ID.toString()));
        }

        @Test
        @DisplayName("гость не найден → 404")
        void notFound_returns404() {
            when(service.linkChatIdByPhone(PHONE, CHAT_ID)).thenReturn(Optional.empty());

            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "phone":  "%s",
                        "chatId": %d
                    }
                """.formatted(PHONE, CHAT_ID))
                .when().post(BASE_PATH + "/link-chat")
                .then()
                .statusCode(404);
        }

        @Test
        @DisplayName("отсутствует phone → 400")
        void missingPhone_returns400() {
            given()
                .contentType(ContentType.JSON)
                .body("""
                    { "chatId": %d }
                """.formatted(CHAT_ID))
                .when().post(BASE_PATH + "/link-chat")
                .then()
                .statusCode(400);

            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("отсутствует chatId → 400")
        void missingChatId_returns400() {
            given()
                .contentType(ContentType.JSON)
                .body("""
                    { "phone": "%s" }
                """.formatted(PHONE))
                .when().post(BASE_PATH + "/link-chat")
                .then()
                .statusCode(400);

            verifyNoInteractions(service);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // POST /coworking/v1/guests/unlink-chat
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /guests/unlink-chat")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class UnlinkChat {

        @Test
        @DisplayName("успешно → 204")
        void success_returns204() {
            doNothing().when(service).unlinkChat(CHAT_ID);

            given()
                .contentType(ContentType.JSON)
                .body("""
                    { "chatId": %d }
                """.formatted(CHAT_ID))
                .when().post(BASE_PATH + "/unlink-chat")
                .then()
                .statusCode(204);

            verify(service).unlinkChat(CHAT_ID);
        }

        @Test
        @DisplayName("отсутствует chatId → 400")
        void missingChatId_returns400() {
            given()
                .contentType(ContentType.JSON)
                .body("{}")
                .when().post(BASE_PATH + "/unlink-chat")
                .then()
                .statusCode(400);

            verifyNoInteractions(service);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /coworking/v1/guests/{externalId}/link-status
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /guests/{externalId}/link-status")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class GetLinkStatus {

        @Test
        @DisplayName("статус CONFIRMED → 200 linked=true")
        void confirmed_returnsLinked() {
            UUID id = UUID.fromString("cc100000-0000-0000-0000-000000000001");
            when(service.get(id)).thenReturn(new CoworkingGuest(id, null, "A", "B", "ab", "+70000000010"));

            given().contentType(ContentType.JSON)
                .when().post(BASE_PATH + "/" + id + "/link-init")
                .then().statusCode(204);
            given().contentType(ContentType.JSON)
                .when().post(BASE_PATH + "/" + id + "/link-confirm")
                .then().statusCode(204);

            given()
                .when().get(BASE_PATH + "/" + id + "/link-status")
                .then()
                .statusCode(200)
                .body("linked",   equalTo(true))
                .body("rejected", equalTo(false))
                .body("conflict", equalTo(false));
        }

        @Test
        @DisplayName("статус REJECTED → 200 rejected=true")
        void rejected_returnsRejected() {
            UUID id = UUID.fromString("cc200000-0000-0000-0000-000000000002");
            when(service.get(id)).thenReturn(new CoworkingGuest(id, null, "A", "B", "ab", "+70000000011"));

            given().contentType(ContentType.JSON)
                .when().post(BASE_PATH + "/" + id + "/link-init")
                .then().statusCode(204);
            given().contentType(ContentType.JSON)
                .when().post(BASE_PATH + "/" + id + "/link-reject")
                .then().statusCode(204);

            given()
                .when().get(BASE_PATH + "/" + id + "/link-status")
                .then()
                .statusCode(200)
                .body("linked",   equalTo(false))
                .body("rejected", equalTo(true))
                .body("conflict", equalTo(false));
        }

        @Test
        @DisplayName("статус PENDING → 200 все поля false")
        void pending_returnsAllFalse() {
            UUID id = UUID.fromString("cc300000-0000-0000-0000-000000000003");
            when(service.get(id)).thenReturn(new CoworkingGuest(id, null, "A", "B", "ab", "+70000000012"));

            given().contentType(ContentType.JSON)
                .when().post(BASE_PATH + "/" + id + "/link-init")
                .then().statusCode(204);

            given()
                .when().get(BASE_PATH + "/" + id + "/link-status")
                .then()
                .statusCode(200)
                .body("linked",   equalTo(false))
                .body("rejected", equalTo(false))
                .body("conflict", equalTo(false));
        }

        @Test
        @DisplayName("сессия не инициализирована → 404")
        void noSession_returns404() {
            UUID id = UUID.fromString("cc400000-0000-0000-0000-000000000004");
            when(service.get(id)).thenReturn(new CoworkingGuest(id, null, "A", "B", "ab", "+70000000013"));

            given()
                .when().get(BASE_PATH + "/" + id + "/link-status")
                .then()
                .statusCode(404);
        }

        @Test
        @DisplayName("гость не найден → 404")
        void guestNotFound_returns404() {
            UUID id = UUID.fromString("cc500000-0000-0000-0000-000000000005");
            when(service.get(id))
                .thenThrow(new ResourceNotFoundException("CoworkingGuest", id));

            given()
                .when().get(BASE_PATH + "/" + id + "/link-status")
                .then()
                .statusCode(404);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // POST /coworking/v1/guests/{externalId}/link-init
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /guests/{externalId}/link-init")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class LinkInit {

        @Test
        @DisplayName("успешно → 204")
        void success_returns204() {
            when(service.get(GUEST_ID)).thenReturn(guest());

            given()
                .contentType(ContentType.JSON)
                .when().post(BASE_PATH + "/" + GUEST_ID + "/link-init")
                .then()
                .statusCode(204);
        }

        @Test
        @DisplayName("гость не найден → 404")
        void guestNotFound_returns404() {
            when(service.get(GUEST_ID))
                .thenThrow(new ResourceNotFoundException("CoworkingGuest", GUEST_ID));

            given()
                .contentType(ContentType.JSON)
                .when().post(BASE_PATH + "/" + GUEST_ID + "/link-init")
                .then()
                .statusCode(404);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // POST /coworking/v1/guests/{externalId}/link-confirm
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /guests/{externalId}/link-confirm")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class LinkConfirm {

        @Test
        @DisplayName("успешно → 204")
        void success_returns204() {
            when(service.get(GUEST_ID)).thenReturn(guest());

            given()
                .contentType(ContentType.JSON)
                .when().post(BASE_PATH + "/" + GUEST_ID + "/link-confirm")
                .then()
                .statusCode(204);
        }

        @Test
        @DisplayName("гость не найден → 404")
        void guestNotFound_returns404() {
            when(service.get(GUEST_ID))
                .thenThrow(new ResourceNotFoundException("CoworkingGuest", GUEST_ID));

            given()
                .contentType(ContentType.JSON)
                .when().post(BASE_PATH + "/" + GUEST_ID + "/link-confirm")
                .then()
                .statusCode(404);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // POST /coworking/v1/guests/{externalId}/link-reject
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /guests/{externalId}/link-reject")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class LinkReject {

        @Test
        @DisplayName("успешно → 204")
        void success_returns204() {
            when(service.get(GUEST_ID)).thenReturn(guest());

            given()
                .contentType(ContentType.JSON)
                .when().post(BASE_PATH + "/" + GUEST_ID + "/link-reject")
                .then()
                .statusCode(204);
        }

        @Test
        @DisplayName("гость не найден → 404")
        void guestNotFound_returns404() {
            when(service.get(GUEST_ID))
                .thenThrow(new ResourceNotFoundException("CoworkingGuest", GUEST_ID));

            given()
                .contentType(ContentType.JSON)
                .when().post(BASE_PATH + "/" + GUEST_ID + "/link-reject")
                .then()
                .statusCode(404);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /coworking/v1/guests/{externalId}/event-history
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /guests/{externalId}/event-history")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class EventHistory {

        @Test
        @DisplayName("есть записи → 200 массив")
        void withRegistrations_returnsList() {
            Instant eventDate = Instant.parse("2026-03-01T10:00:00Z");
            when(registrationService.findByGuestExternalId(GUEST_ID))
                .thenReturn(List.of(new EventRegistrationItem("Мастер-класс по дереву", eventDate)));

            given()
                .when().get(BASE_PATH + "/" + GUEST_ID + "/event-history")
                .then()
                .statusCode(200)
                .body("$",            hasSize(1))
                .body("[0].eventName", equalTo("Мастер-класс по дереву"));
        }

        @Test
        @DisplayName("нет записей → 200 пустой массив")
        void noRegistrations_returnsEmptyList() {
            when(registrationService.findByGuestExternalId(GUEST_ID)).thenReturn(List.of());

            given()
                .when().get(BASE_PATH + "/" + GUEST_ID + "/event-history")
                .then()
                .statusCode(200)
                .body("$", hasSize(0));
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /coworking/v1/guests/{externalId}/tariff-history
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /guests/{externalId}/tariff-history")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class TariffHistory {

        static final UUID TARIFF_ID      = UUID.fromString("cccccccc-0000-0000-0000-000000000003");
        static final UUID TARIFF_TYPE_ID = UUID.fromString("dddddddd-0000-0000-0000-000000000004");

        @Test
        @DisplayName("есть тарифы → 200 массив")
        void withTariffs_returnsList() {
            var tariff = new CoworkingGuestTariff(TARIFF_ID, TARIFF_TYPE_ID, GUEST_ID, 2, GuestTariffStatus.ACTIVE);
            when(tariffService.findByGuestExternalId(GUEST_ID)).thenReturn(List.of(tariff));

            given()
                .when().get(BASE_PATH + "/" + GUEST_ID + "/tariff-history")
                .then()
                .statusCode(200)
                .body("$",            hasSize(1))
                .body("[0].id",       equalTo(TARIFF_ID.toString()))
                .body("[0].status",   equalTo("ACTIVE"))
                .body("[0].daysUsed", equalTo(2));
        }

        @Test
        @DisplayName("нет тарифов → 200 пустой массив")
        void noTariffs_returnsEmptyList() {
            when(tariffService.findByGuestExternalId(GUEST_ID)).thenReturn(List.of());

            given()
                .when().get(BASE_PATH + "/" + GUEST_ID + "/tariff-history")
                .then()
                .statusCode(200)
                .body("$", hasSize(0));
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // PATCH /coworking/v1/guests/{externalId}
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("PATCH /guests/{externalId}")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class Update {

        @Test
        @DisplayName("валидный запрос → 200 обновлённый гость")
        void validRequest_returns200() {
            CoworkingGuest updated = new CoworkingGuest(GUEST_ID, CHAT_ID, "Petr", "Ivanov", "petr_ivanov", "+79000000000");
            when(service.update(eq(GUEST_ID), any())).thenReturn(updated);

            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "firstName": "Petr",
                        "lastName":  "Ivanov",
                        "phone":     "+79000000000",
                        "telegram":  "petr_ivanov"
                    }
                """)
                .when().patch(BASE_PATH + "/" + GUEST_ID)
                .then()
                .statusCode(200)
                .body("id",        equalTo(GUEST_ID.toString()))
                .body("firstName", equalTo("Petr"))
                .body("lastName",  equalTo("Ivanov"));
        }

        @Test
        @DisplayName("без обязательного firstName → 400")
        void missingFirstName_returns400() {
            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "lastName": "Ivanov",
                        "phone":    "+79000000000"
                    }
                """)
                .when().patch(BASE_PATH + "/" + GUEST_ID)
                .then()
                .statusCode(400);

            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("гость не найден → 404")
        void guestNotFound_returns404() {
            when(service.update(eq(GUEST_ID), any()))
                .thenThrow(new ResourceNotFoundException("CoworkingGuest", GUEST_ID));

            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "firstName": "Petr",
                        "lastName":  "Ivanov",
                        "phone":     "+79000000000"
                    }
                """)
                .when().patch(BASE_PATH + "/" + GUEST_ID)
                .then()
                .statusCode(404)
                .body("code", equalTo("RESOURCE_NOT_FOUND"));
        }
    }
}
