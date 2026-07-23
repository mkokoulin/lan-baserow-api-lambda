package com.lan.app.api.resource;

import com.lan.app.domain.exception.ResourceNotFoundException;
import com.lan.app.domain.model.Festival;
import com.lan.app.domain.model.Id;
import com.lan.app.service.EventsFestivalService;
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;

@QuarkusTest
class EventsFestivalResourceTest {

    static final String BASE_PATH = "/events/v1/festivals";
    static final UUID FESTIVAL_ID = UUID.fromString("33333333-4444-0000-0000-000000000001");

    @InjectMock
    EventsFestivalService service;

    static Festival festival() {
        return new Festival(
            new Id(1, FESTIVAL_ID), "Summer Fest 2026", "desc", List.of(),
            Instant.parse("2026-07-01T10:00:00Z"), Instant.parse("2026-07-05T22:00:00Z"),
            true, null, true, null
        );
    }

    @Nested
    @DisplayName("GET /events/v1/festivals")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class List_ {

        @Test
        @DisplayName("есть фестивали → 200 массив")
        void withFestivals_returnsList() {
            when(service.list()).thenReturn(List.of(festival()));

            given()
                .when().get(BASE_PATH)
                .then()
                .statusCode(200)
                .body("$",       hasSize(1))
                .body("[0].name", equalTo("Summer Fest 2026"));
        }

        @Test
        @DisplayName("нет фестивалей → 200 пустой массив")
        void noFestivals_returnsEmptyList() {
            when(service.list()).thenReturn(List.of());

            given()
                .when().get(BASE_PATH)
                .then()
                .statusCode(200)
                .body("$", hasSize(0));
        }
    }

    @Nested
    @DisplayName("GET /events/v1/festivals/{externalId}")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class GetById {

        @Test
        @DisplayName("фестиваль найден → 200")
        void found_returns200() {
            when(service.get(FESTIVAL_ID)).thenReturn(festival());

            given()
                .when().get(BASE_PATH + "/" + FESTIVAL_ID)
                .then()
                .statusCode(200)
                .body("id",   equalTo(FESTIVAL_ID.toString()))
                .body("name", equalTo("Summer Fest 2026"));
        }

        @Test
        @DisplayName("фестиваль не найден → 404 RESOURCE_NOT_FOUND")
        void notFound_returns404() {
            when(service.get(FESTIVAL_ID)).thenThrow(new ResourceNotFoundException("Festival", FESTIVAL_ID));

            given()
                .when().get(BASE_PATH + "/" + FESTIVAL_ID)
                .then()
                .statusCode(404)
                .body("code", equalTo("RESOURCE_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("GET /events/v1/festivals — без авторизации")
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
