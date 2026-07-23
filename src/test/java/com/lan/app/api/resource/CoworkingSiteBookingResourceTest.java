package com.lan.app.api.resource;

import com.lan.app.domain.model.CoworkingSiteBooking;
import com.lan.app.service.CoworkingSiteBookingService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@QuarkusTest
class CoworkingSiteBookingResourceTest {

    static final String BASE_PATH = "/coworking/v1/site-bookings";

    @InjectMock
    CoworkingSiteBookingService service;

    static final UUID EXTERNAL_ID = UUID.fromString("bbbbbbbb-0000-0000-0000-000000000001");

    static CoworkingSiteBooking booking() {
        return new CoworkingSiteBooking(EXTERNAL_ID, "Ivan", "+79161234567", "ivan_petrov",
            "day-pass", "2026-08-01", "10:00", "18:00");
    }

    static String validBody() {
        return """
            {
                "externalId":  "%s",
                "firstName":   "Ivan",
                "phone":       "+79161234567",
                "telegram":    "ivan_petrov",
                "tariff":      "day-pass",
                "bookingDate": "2026-08-01",
                "startTime":   "10:00",
                "endTime":     "18:00"
            }
        """.formatted(EXTERNAL_ID);
    }

    @Nested
    @DisplayName("POST /coworking/v1/site-bookings")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class Create {

        @Test
        @DisplayName("валидный запрос → 201 с Location и статусом")
        void validRequest_returns201() {
            when(service.create(any())).thenReturn(booking());

            given()
                .contentType(ContentType.JSON)
                .body(validBody())
                .when().post(BASE_PATH)
                .then()
                .statusCode(201)
                .header("Location", containsString(EXTERNAL_ID.toString()))
                .body("id",     equalTo(EXTERNAL_ID.toString()))
                .body("status", equalTo("Новая"));
        }

        @Test
        @DisplayName("отсутствует firstName → 400")
        void missingFirstName_returns400() {
            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "externalId":  "%s",
                        "phone":       "+79161234567",
                        "tariff":      "day-pass",
                        "bookingDate": "2026-08-01",
                        "startTime":   "10:00",
                        "endTime":     "18:00"
                    }
                """.formatted(EXTERNAL_ID))
                .when().post(BASE_PATH)
                .then()
                .statusCode(400);

            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("отсутствует externalId → 400")
        void missingExternalId_returns400() {
            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "firstName":   "Ivan",
                        "phone":       "+79161234567",
                        "tariff":      "day-pass",
                        "bookingDate": "2026-08-01",
                        "startTime":   "10:00",
                        "endTime":     "18:00"
                    }
                """)
                .when().post(BASE_PATH)
                .then()
                .statusCode(400);

            verifyNoInteractions(service);
        }
    }

    @Nested
    @DisplayName("POST /coworking/v1/site-bookings — без авторизации")
    class Unauthenticated {

        @Test
        @DisplayName("без JWT → 401")
        void unauthenticated_returns401() {
            given()
                .contentType(ContentType.JSON)
                .body(validBody())
                .when().post(BASE_PATH)
                .then()
                .statusCode(401);
        }
    }
}
