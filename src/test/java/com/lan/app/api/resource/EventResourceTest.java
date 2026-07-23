package com.lan.app.api.resource;

import com.lan.app.domain.exception.ResourceNotFoundException;
import com.lan.app.domain.model.Event;
import com.lan.app.domain.model.Id;
import com.lan.app.service.EventService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;

@QuarkusTest
class EventResourceTest {

    static final String BASE_PATH = "/events/v1";
    static final UUID EVENT_ID = UUID.fromString("22222222-3333-0000-0000-000000000001");

    @InjectMock
    EventService service;

    static Event event() {
        return new Event(
            new Id(1, EVENT_ID), "Мастер-класс",
            Instant.parse("2026-05-10T18:00:00Z"), Instant.parse("2026-05-10T21:00:00Z"),
            "desc", null, null, null, null,
            true, List.of(), null, null,
            true, true, false, BigDecimal.ZERO, null, 50, false
        );
    }

    @Nested
    @DisplayName("GET /events/v1")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class List_ {

        @Test
        @DisplayName("есть события → 200 массив")
        void withEvents_returnsList() {
            when(service.list()).thenReturn(List.of(event()));

            given()
                .when().get(BASE_PATH)
                .then()
                .statusCode(200)
                .body("$",       hasSize(1))
                .body("[0].name", equalTo("Мастер-класс"));
        }

        @Test
        @DisplayName("нет событий → 200 пустой массив")
        void noEvents_returnsEmptyList() {
            when(service.list()).thenReturn(List.of());

            given()
                .when().get(BASE_PATH)
                .then()
                .statusCode(200)
                .body("$", hasSize(0));
        }
    }

    @Nested
    @DisplayName("GET /events/v1/{externalId}")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class GetById {

        @Test
        @DisplayName("событие найдено → 200")
        void found_returns200() {
            when(service.get(EVENT_ID)).thenReturn(event());

            given()
                .when().get(BASE_PATH + "/" + EVENT_ID)
                .then()
                .statusCode(200)
                .body("id",   equalTo(EVENT_ID.toString()))
                .body("name", equalTo("Мастер-класс"));
        }

        @Test
        @DisplayName("событие не найдено → 404 RESOURCE_NOT_FOUND")
        void notFound_returns404() {
            when(service.get(EVENT_ID)).thenThrow(new ResourceNotFoundException("Event", EVENT_ID));

            given()
                .when().get(BASE_PATH + "/" + EVENT_ID)
                .then()
                .statusCode(404)
                .body("code", equalTo("RESOURCE_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("GET /events/v1 — без авторизации")
    class Unauthenticated {

        @Test
        @DisplayName("без JWT → 401")
        void unauthenticated_returns401() {
            given()
                .when().get(BASE_PATH)
                .then()
                .statusCode(401);
        }
    }
}
