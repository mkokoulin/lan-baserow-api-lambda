package com.lan.app.api.resource;

import com.lan.app.domain.exception.ResourceNotFoundException;
import com.lan.app.domain.model.CoworkingGuestTariff;
import com.lan.app.domain.model.GuestTariffStatus;
import com.lan.app.service.CoworkingGuestTariffService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
class CoworkingGuestTariffResourceTest {

    static final String BASE_PATH = "/coworking/v1/guest-tariffs";

    @InjectMock
    CoworkingGuestTariffService service;

    // ─────────────────────────────────────────────────────────────────
    // Test fixtures
    // ─────────────────────────────────────────────────────────────────

    static final UUID TARIFF_EXT_ID  = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");
    static final UUID GUEST_EXT_ID   = UUID.fromString("bbbbbbbb-0000-0000-0000-000000000002");
    static final UUID TARIFF_TYPE_ID = UUID.fromString("cccccccc-0000-0000-0000-000000000003");

    static CoworkingGuestTariff activeTariff() {
        return new CoworkingGuestTariff(
            TARIFF_EXT_ID,
            TARIFF_TYPE_ID,
            GUEST_EXT_ID,
            3,
            GuestTariffStatus.ACTIVE
        );
    }

    static CoworkingGuestTariff pendingTariff() {
        return new CoworkingGuestTariff(
            TARIFF_EXT_ID,
            TARIFF_TYPE_ID,
            GUEST_EXT_ID,
            0,
            GuestTariffStatus.PENDING
        );
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /coworking/v1/guest-tariffs?guestId=…
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /guest-tariffs")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class ListEndpoint {

        @Test
        @DisplayName("без guestId → 200 пустой список")
        void withoutGuestId_returnsEmptyList() {
            given()
                .when().get(BASE_PATH)
                .then()
                .statusCode(200)
                .body("$", hasSize(0));

            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("с guestId → 200 список тарифов")
        void withGuestId_returnsTariffList() {
            when(service.findByGuestExternalId(GUEST_EXT_ID))
                .thenReturn(List.of(activeTariff()));

            given()
                .queryParam("guestId", GUEST_EXT_ID.toString())
                .when().get(BASE_PATH)
                .then()
                .statusCode(200)
                .body("$",           hasSize(1))
                .body("[0].id",      equalTo(TARIFF_EXT_ID.toString()))
                .body("[0].guestId", equalTo(GUEST_EXT_ID.toString()))
                .body("[0].status",  equalTo("ACTIVE"))
                .body("[0].daysUsed", equalTo(3));
        }

        @Test
        @DisplayName("сервис вернул пустой список → 200 пустой массив")
        void withGuestId_noTariffs_returnsEmptyList() {
            when(service.findByGuestExternalId(GUEST_EXT_ID)).thenReturn(List.of());

            given()
                .queryParam("guestId", GUEST_EXT_ID.toString())
                .when().get(BASE_PATH)
                .then()
                .statusCode(200)
                .body("$", hasSize(0));
        }
    }

    @Nested
    @DisplayName("GET /guest-tariffs — без авторизации")
    class ListUnauthenticated {

        @Test
        @DisplayName("без JWT → 401")
        void unauthenticated_returns401() {
            given()
                .when().get(BASE_PATH)
                .then()
                .statusCode(401);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // POST /coworking/v1/guest-tariffs
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /guest-tariffs")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class Create {

        @Test
        @DisplayName("валидный запрос → 200, статус PENDING")
        void validRequest_returnsPendingTariff() {
            when(service.create(GUEST_EXT_ID, TARIFF_TYPE_ID)).thenReturn(pendingTariff());

            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "guestId":  "%s",
                        "tariffId": "%s"
                    }
                """.formatted(GUEST_EXT_ID, TARIFF_TYPE_ID))
                .when().post(BASE_PATH)
                .then()
                .statusCode(200)
                .body("id",       equalTo(TARIFF_EXT_ID.toString()))
                .body("status",   equalTo("PENDING"))
                .body("daysUsed", equalTo(0));
        }

        @Test
        @DisplayName("отсутствует guestId → 400")
        void missingGuestId_returns400() {
            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "tariffId": "%s"
                    }
                """.formatted(TARIFF_TYPE_ID))
                .when().post(BASE_PATH)
                .then()
                .statusCode(400);

            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("отсутствует tariffId → 400")
        void missingTariffId_returns400() {
            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "guestId": "%s"
                    }
                """.formatted(GUEST_EXT_ID))
                .when().post(BASE_PATH)
                .then()
                .statusCode(400);

            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("пустое тело → 400")
        void emptyBody_returns400() {
            given()
                .contentType(ContentType.JSON)
                .body("{}")
                .when().post(BASE_PATH)
                .then()
                .statusCode(400);

            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("гость не найден → 404 RESOURCE_NOT_FOUND")
        void guestNotFound_returns404() {
            when(service.create(any(), any()))
                .thenThrow(new ResourceNotFoundException("CoworkingGuest", GUEST_EXT_ID));

            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "guestId":  "%s",
                        "tariffId": "%s"
                    }
                """.formatted(GUEST_EXT_ID, TARIFF_TYPE_ID))
                .when().post(BASE_PATH)
                .then()
                .statusCode(404)
                .body("code", equalTo("RESOURCE_NOT_FOUND"));
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /coworking/v1/guest-tariffs/{externalId}
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /guest-tariffs/{externalId}")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class GetById {

        @Test
        @DisplayName("существующий тариф → 200 со всеми полями")
        void existingTariff_returnsFullResponse() {
            when(service.get(TARIFF_EXT_ID)).thenReturn(activeTariff());

            given()
                .when().get(BASE_PATH + "/" + TARIFF_EXT_ID)
                .then()
                .statusCode(200)
                .body("id",        equalTo(TARIFF_EXT_ID.toString()))
                .body("tariffId",  equalTo(TARIFF_TYPE_ID.toString()))
                .body("guestId",   equalTo(GUEST_EXT_ID.toString()))
                .body("daysUsed",  equalTo(3))
                .body("status",    equalTo("ACTIVE"));
        }

        @Test
        @DisplayName("тариф не найден → 404 RESOURCE_NOT_FOUND")
        void notFound_returns404() {
            when(service.get(TARIFF_EXT_ID))
                .thenThrow(new ResourceNotFoundException("CoworkingGuestTariff", TARIFF_EXT_ID));

            given()
                .when().get(BASE_PATH + "/" + TARIFF_EXT_ID)
                .then()
                .statusCode(404)
                .body("code",               equalTo("RESOURCE_NOT_FOUND"))
                .body("details.externalId", equalTo(TARIFF_EXT_ID.toString()));
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
    // POST /coworking/v1/guest-tariffs/{externalId}/deduct-day
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /guest-tariffs/{externalId}/deduct-day")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class DeductDay {

        @Test
        @DisplayName("успешное списание → 200, daysUsed увеличен на 1")
        void success_returnsTariffWithIncrementedDaysUsed() {
            var updated = new CoworkingGuestTariff(
                TARIFF_EXT_ID, TARIFF_TYPE_ID, GUEST_EXT_ID,
                4, GuestTariffStatus.ACTIVE
            );
            when(service.deductDay(TARIFF_EXT_ID)).thenReturn(updated);

            given()
                .contentType(ContentType.JSON)
                .when().post(BASE_PATH + "/" + TARIFF_EXT_ID + "/deduct-day")
                .then()
                .statusCode(200)
                .body("daysUsed", equalTo(4))
                .body("status",   equalTo("ACTIVE"));
        }

        @Test
        @DisplayName("тариф не найден → 404 RESOURCE_NOT_FOUND")
        void notFound_returns404() {
            when(service.deductDay(TARIFF_EXT_ID))
                .thenThrow(new ResourceNotFoundException("CoworkingGuestTariff", TARIFF_EXT_ID));

            given()
                .contentType(ContentType.JSON)
                .when().post(BASE_PATH + "/" + TARIFF_EXT_ID + "/deduct-day")
                .then()
                .statusCode(404)
                .body("code", equalTo("RESOURCE_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("POST /guest-tariffs/{externalId}/deduct-day — роли")
    @TestSecurity(user = "admin-user", roles = {"admin"})
    class DeductDayAdmin {

        @Test
        @DisplayName("роль admin тоже разрешена → 200")
        void adminRole_isAllowed() {
            when(service.deductDay(TARIFF_EXT_ID)).thenReturn(activeTariff());

            given()
                .contentType(ContentType.JSON)
                .when().post(BASE_PATH + "/" + TARIFF_EXT_ID + "/deduct-day")
                .then()
                .statusCode(200);
        }
    }
}
