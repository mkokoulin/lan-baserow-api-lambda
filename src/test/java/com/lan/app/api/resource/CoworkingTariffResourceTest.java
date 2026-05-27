package com.lan.app.api.resource;

import com.lan.app.domain.exception.ResourceNotFoundException;
import com.lan.app.domain.model.CoworkingTariff;
import com.lan.app.domain.model.CoworkingTariffType;
import com.lan.app.service.CoworkingTariffService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class CoworkingTariffResourceTest {

    static final String BASE_PATH = "/coworking/v1/tariffs";

    @InjectMock
    CoworkingTariffService service;

    static final UUID TARIFF_ID = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");

    static CoworkingTariff tariff() {
        return new CoworkingTariff(
            TARIFF_ID, "Day Pass", 1500, "2 hours", false, true, false, true, CoworkingTariffType.SHORT
        );
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /coworking/v1/tariffs
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /tariffs")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class List_ {

        @Test
        @DisplayName("список тарифов → 200 массив")
        void returnsTariffList() {
            when(service.list()).thenReturn(List.of(tariff()));

            given()
                .when().get(BASE_PATH)
                .then()
                .statusCode(200)
                .body("$",                  hasSize(1))
                .body("[0].id",             equalTo(TARIFF_ID.toString()))
                .body("[0].name",           equalTo("Day Pass"))
                .body("[0].price",          equalTo(1500))
                .body("[0].meetingRoom",    equalTo("2 hours"))
                .body("[0].fixedDesk",      equalTo(false))
                .body("[0].filterCoffeeAndTea", equalTo(true))
                .body("[0].printoutScan",   equalTo(false))
                .body("[0].luggageStorage", equalTo(true))
                .body("[0].type",           equalTo("SHORT"));
        }

        @Test
        @DisplayName("пустой список → 200 пустой массив")
        void emptyList_returns200() {
            when(service.list()).thenReturn(List.of());

            given()
                .when().get(BASE_PATH)
                .then()
                .statusCode(200)
                .body("$", hasSize(0));
        }
    }

    @Nested
    @DisplayName("GET /tariffs — без авторизации")
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
    // GET /coworking/v1/tariffs/{externalId}
    // ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /tariffs/{externalId}")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class GetById {

        @Test
        @DisplayName("существующий тариф → 200 со всеми полями")
        void existingTariff_returnsFullResponse() {
            when(service.get(TARIFF_ID)).thenReturn(tariff());

            given()
                .when().get(BASE_PATH + "/" + TARIFF_ID)
                .then()
                .statusCode(200)
                .body("id",    equalTo(TARIFF_ID.toString()))
                .body("name",  equalTo("Day Pass"))
                .body("price", equalTo(1500))
                .body("type",  equalTo("SHORT"));
        }

        @Test
        @DisplayName("тариф не найден → 404 RESOURCE_NOT_FOUND")
        void notFound_returns404() {
            when(service.get(TARIFF_ID))
                .thenThrow(new ResourceNotFoundException("CoworkingTariff", TARIFF_ID));

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
