package com.lan.app.api.resource;

import com.lan.app.domain.exception.ResourceNotFoundException;
import com.lan.app.domain.model.CoworkingActiveTariff;
import com.lan.app.domain.model.CoworkingActiveTariffListItem;
import com.lan.app.service.CoworkingActiveTariffService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class CoworkingActiveTariffResourceTest {

    static final String BASE_PATH = "/coworking/v1/active-tariffs";

    @InjectMock
    CoworkingActiveTariffService service;

    static final UUID TARIFF_ID  = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");
    static final UUID GUEST_ID   = UUID.fromString("bbbbbbbb-0000-0000-0000-000000000002");
    static final UUID TARIFF_DEF = UUID.fromString("cccccccc-0000-0000-0000-000000000003");

    static final Instant DATE_START = Instant.parse("2026-01-01T00:00:00Z");
    static final Instant DATE_END   = Instant.parse("2026-12-31T23:59:59Z");

    static CoworkingActiveTariff activeTariff() {
        return new CoworkingActiveTariff(TARIFF_ID, GUEST_ID, 5, DATE_START, DATE_END, TARIFF_DEF);
    }

    static CoworkingActiveTariffListItem listItem() {
        return new CoworkingActiveTariffListItem(TARIFF_ID, 5, DATE_START, DATE_END);
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /coworking/v1/active-tariffs
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /active-tariffs")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class List_ {

        @Test
        @DisplayName("список тарифов → 200 массив облегчённых объектов")
        void returnsList() {
            when(service.list()).thenReturn(List.of(listItem()));

            given()
                .when().get(BASE_PATH)
                .then()
                .statusCode(200)
                .body("$",            hasSize(1))
                .body("[0].id",       equalTo(TARIFF_ID.toString()))
                .body("[0].daysUsed", equalTo(5));
        }

        @Test
        @DisplayName("нет тарифов → 200 пустой массив")
        void empty_returns200EmptyArray() {
            when(service.list()).thenReturn(List.of());

            given()
                .when().get(BASE_PATH)
                .then()
                .statusCode(200)
                .body("$", hasSize(0));
        }
    }

    @Nested
    @DisplayName("GET /active-tariffs — без авторизации")
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
    // GET /coworking/v1/active-tariffs/{externalId}
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /active-tariffs/{externalId}")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class GetById {

        @Test
        @DisplayName("существующий тариф → 200 со всеми полями")
        void existingTariff_returnsFullResponse() {
            when(service.get(TARIFF_ID)).thenReturn(activeTariff());

            given()
                .when().get(BASE_PATH + "/" + TARIFF_ID)
                .then()
                .statusCode(200)
                .body("id",       equalTo(TARIFF_ID.toString()))
                .body("guestId",  equalTo(GUEST_ID.toString()))
                .body("tariffId", equalTo(TARIFF_DEF.toString()))
                .body("daysUsed", equalTo(5));
        }

        @Test
        @DisplayName("тариф не найден → 404 RESOURCE_NOT_FOUND")
        void notFound_returns404() {
            when(service.get(TARIFF_ID))
                .thenThrow(new ResourceNotFoundException("CoworkingActiveTariff", TARIFF_ID));

            given()
                .when().get(BASE_PATH + "/" + TARIFF_ID)
                .then()
                .statusCode(404)
                .body("code",               equalTo("RESOURCE_NOT_FOUND"))
                .body("details.externalId", equalTo(TARIFF_ID.toString()));
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
}
