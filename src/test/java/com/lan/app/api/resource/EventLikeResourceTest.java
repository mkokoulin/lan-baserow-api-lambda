package com.lan.app.api.resource;

import java.util.UUID;

import com.lan.app.service.EventLikeService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@QuarkusTest
class EventLikeResourceTest {

    static final UUID EVENT_ID = UUID.fromString("22222222-3333-0000-0000-000000000001");
    static final String BASE_PATH = "/events/v1/" + EVENT_ID + "/likes";

    @InjectMock
    EventLikeService service;

    @Nested
    @DisplayName("POST /events/v1/{externalId}/likes")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class Like {

        @Test
        @DisplayName("валидный anonId → 200 с актуальным count")
        void validRequest_returnsCount() {
            when(service.like(EVENT_ID, "anon-123")).thenReturn(4L);

            given()
                .contentType(ContentType.JSON)
                .body("""
                    { "anonId": "anon-123" }
                """)
                .when().post(BASE_PATH)
                .then()
                .statusCode(200)
                .body("count", equalTo(4));
        }

        @Test
        @DisplayName("отсутствует anonId → 400")
        void missingAnonId_returns400() {
            given()
                .contentType(ContentType.JSON)
                .body("{}")
                .when().post(BASE_PATH)
                .then()
                .statusCode(400);

            verifyNoInteractions(service);
        }
    }

    @Nested
    @DisplayName("DELETE /events/v1/{externalId}/likes")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class Unlike {

        @Test
        @DisplayName("валидный anonId → 200 с актуальным count")
        void validRequest_returnsCount() {
            when(service.unlike(EVENT_ID, "anon-123")).thenReturn(0L);

            given()
                .queryParam("anonId", "anon-123")
                .when().delete(BASE_PATH)
                .then()
                .statusCode(200)
                .body("count", equalTo(0));
        }
    }

    @Nested
    @DisplayName("POST /events/v1/{externalId}/likes — без авторизации")
    class Unauthenticated {

        @Test
        @DisplayName("без JWT → 401")
        void unauthenticated_returns401() {
            given()
                .contentType(ContentType.JSON)
                .body("""
                    { "anonId": "anon-123" }
                """)
                .when().post(BASE_PATH)
                .then()
                .statusCode(401);
        }
    }
}
