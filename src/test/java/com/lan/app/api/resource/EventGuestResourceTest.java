package com.lan.app.api.resource;

import com.lan.app.domain.model.EventGuest;
import com.lan.app.domain.model.Id;
import com.lan.app.service.EventGuestService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@QuarkusTest
class EventGuestResourceTest {

    static final String BASE_PATH = "/events/v1/guests";
    static final UUID GUEST_EXTERNAL_ID = UUID.fromString("aaaaaaaa-1111-0000-0000-000000000001");

    @InjectMock
    EventGuestService service;

    static EventGuest guest() {
        return new EventGuest(new Id(1, GUEST_EXTERNAL_ID), "Ivan", "Petrov", "ivan", "+79161234567", "website", null);
    }

    @Nested
    @DisplayName("POST /events/v1/guests")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class Create {

        @Test
        @DisplayName("валидный запрос → 201 с Location")
        void validRequest_returns201() {
            when(service.create("Ivan", "Petrov", "+79161234567", "ivan", "website", null)).thenReturn(guest());

            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "first_name": "Ivan",
                        "last_name":  "Petrov",
                        "phone":      "+79161234567",
                        "telegram":   "ivan",
                        "source":     "website"
                    }
                """)
                .when().post(BASE_PATH)
                .then()
                .statusCode(201)
                .header("Location", containsString(GUEST_EXTERNAL_ID.toString()))
                .body("id",        equalTo(GUEST_EXTERNAL_ID.toString()))
                .body("firstName", equalTo("Ivan"));
        }

        @Test
        @DisplayName("отсутствует first_name → 400")
        void missingFirstName_returns400() {
            given()
                .contentType(ContentType.JSON)
                .body("""
                    { "phone": "+79161234567" }
                """)
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
                    { "first_name": "Ivan" }
                """)
                .when().post(BASE_PATH)
                .then()
                .statusCode(400);

            verifyNoInteractions(service);
        }
    }

    @Nested
    @DisplayName("POST /events/v1/guests — без авторизации")
    class Unauthenticated {

        @Test
        @DisplayName("без JWT → 401")
        void unauthenticated_returns401() {
            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "first_name": "Ivan",
                        "phone":      "+79161234567"
                    }
                """)
                .when().post(BASE_PATH)
                .then()
                .statusCode(401);
        }
    }
}
