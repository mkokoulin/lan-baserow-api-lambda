package com.lan.app.api.resource;

import com.lan.app.domain.model.CoworkingNotification;
import com.lan.app.service.CoworkingNotificationService;
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
class CoworkingNotificationResourceTest {

    static final String BASE_PATH = "/coworking/v1/notifications";

    @InjectMock
    CoworkingNotificationService service;

    static CoworkingNotification item() {
        return new CoworkingNotification(
            UUID.fromString("eeeeeeee-0000-0000-0000-000000000001"),
            "Your booking starts in 15 minutes",
            Instant.parse("2026-04-18T09:45:00Z")
        );
    }

    @Nested
    @DisplayName("GET /coworking/v1/notifications")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class List_ {

        @Test
        @DisplayName("есть уведомления → 200 массив")
        void withNotifications_returnsList() {
            when(service.list()).thenReturn(List.of(item()));

            given()
                .when().get(BASE_PATH)
                .then()
                .statusCode(200)
                .body("$",          hasSize(1))
                .body("[0].message", equalTo("Your booking starts in 15 minutes"));
        }

        @Test
        @DisplayName("нет уведомлений → 200 пустой массив")
        void noNotifications_returnsEmptyList() {
            when(service.list()).thenReturn(List.of());

            given()
                .when().get(BASE_PATH)
                .then()
                .statusCode(200)
                .body("$", hasSize(0));
        }
    }

    @Nested
    @DisplayName("GET /coworking/v1/notifications — без авторизации")
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
