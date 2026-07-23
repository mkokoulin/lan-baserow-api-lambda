package com.lan.app.api.resource;

import com.lan.app.domain.exception.ResourceNotFoundException;
import com.lan.app.domain.model.CoworkingMeetingRoomBooking;
import com.lan.app.service.CoworkingMeetingRoomBookingService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@QuarkusTest
class CoworkingMeetingRoomBookingResourceTest {

    static final String BASE_PATH = "/coworking/v1/meeting-room-booking";
    static final UUID BOOKING_ID = UUID.fromString("44444444-5555-0000-0000-000000000001");
    static final UUID GUEST_ID = UUID.fromString("55555555-6666-0000-0000-000000000001");

    @InjectMock
    CoworkingMeetingRoomBookingService service;

    static CoworkingMeetingRoomBooking booking() {
        return new CoworkingMeetingRoomBooking(BOOKING_ID, GUEST_ID,
            Instant.parse("2026-04-20T09:00:00Z"), Instant.parse("2026-04-20T11:00:00Z"),
            4, "Client presentation");
    }

    static String validCreateBody() {
        return """
            {
                "guestId":   "%s",
                "dateStart": "2026-04-20T09:00:00Z",
                "dateEnd":   "2026-04-20T11:00:00Z",
                "persons":   4,
                "comment":   "Client presentation"
            }
        """.formatted(GUEST_ID);
    }

    @Nested
    @DisplayName("GET /coworking/v1/meeting-room-booking/{externalId}")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class GetById {

        @Test
        @DisplayName("бронь найдена → 200")
        void found_returns200() {
            when(service.get(BOOKING_ID)).thenReturn(booking());

            given()
                .when().get(BASE_PATH + "/" + BOOKING_ID)
                .then()
                .statusCode(200)
                .body("id",      equalTo(BOOKING_ID.toString()))
                .body("guestId", equalTo(GUEST_ID.toString()))
                .body("persons", equalTo(4));
        }

        @Test
        @DisplayName("бронь не найдена → 404 RESOURCE_NOT_FOUND")
        void notFound_returns404() {
            when(service.get(BOOKING_ID)).thenThrow(new ResourceNotFoundException("CoworkingMeetingRoomBooking", BOOKING_ID));

            given()
                .when().get(BASE_PATH + "/" + BOOKING_ID)
                .then()
                .statusCode(404)
                .body("code", equalTo("RESOURCE_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("POST /coworking/v1/meeting-room-booking")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class Create {

        @Test
        @DisplayName("валидный запрос → 201 с Location")
        void validRequest_returns201() {
            when(service.create(any())).thenReturn(booking());

            given()
                .contentType(ContentType.JSON)
                .body(validCreateBody())
                .when().post(BASE_PATH)
                .then()
                .statusCode(201)
                .header("Location", containsString(BOOKING_ID.toString()))
                .body("id", equalTo(BOOKING_ID.toString()));
        }

        @Test
        @DisplayName("отсутствует guestId → 400")
        void missingGuestId_returns400() {
            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "dateStart": "2026-04-20T09:00:00Z",
                        "dateEnd":   "2026-04-20T11:00:00Z",
                        "persons":   4
                    }
                """)
                .when().post(BASE_PATH)
                .then()
                .statusCode(400);

            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("отсутствует persons → 400")
        void missingPersons_returns400() {
            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "guestId":   "%s",
                        "dateStart": "2026-04-20T09:00:00Z",
                        "dateEnd":   "2026-04-20T11:00:00Z"
                    }
                """.formatted(GUEST_ID))
                .when().post(BASE_PATH)
                .then()
                .statusCode(400);

            verifyNoInteractions(service);
        }
    }

    @Nested
    @DisplayName("PATCH /coworking/v1/meeting-room-booking/{externalId}")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class Update {

        @Test
        @DisplayName("валидный запрос → 200 обновлённая бронь")
        void validRequest_returns200() {
            var updated = new CoworkingMeetingRoomBooking(BOOKING_ID, GUEST_ID,
                Instant.parse("2026-04-21T09:00:00Z"), Instant.parse("2026-04-21T11:00:00Z"),
                6, "Updated");
            when(service.update(eq(BOOKING_ID), any())).thenReturn(updated);

            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "dateStart": "2026-04-21T09:00:00Z",
                        "dateEnd":   "2026-04-21T11:00:00Z",
                        "persons":   6,
                        "comment":   "Updated"
                    }
                """)
                .when().patch(BASE_PATH + "/" + BOOKING_ID)
                .then()
                .statusCode(200)
                .body("persons", equalTo(6))
                .body("comment", equalTo("Updated"));
        }

        @Test
        @DisplayName("persons < 1 → 400")
        void invalidPersons_returns400() {
            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "dateStart": "2026-04-21T09:00:00Z",
                        "dateEnd":   "2026-04-21T11:00:00Z",
                        "persons":   0
                    }
                """)
                .when().patch(BASE_PATH + "/" + BOOKING_ID)
                .then()
                .statusCode(400);

            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("бронь не найдена → 404 RESOURCE_NOT_FOUND")
        void notFound_returns404() {
            when(service.update(eq(BOOKING_ID), any()))
                .thenThrow(new ResourceNotFoundException("CoworkingMeetingRoomBooking", BOOKING_ID));

            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "dateStart": "2026-04-21T09:00:00Z",
                        "dateEnd":   "2026-04-21T11:00:00Z",
                        "persons":   6
                    }
                """)
                .when().patch(BASE_PATH + "/" + BOOKING_ID)
                .then()
                .statusCode(404)
                .body("code", equalTo("RESOURCE_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("GET /coworking/v1/meeting-room-booking/{externalId} — без авторизации")
    class Unauthenticated {

        @Test
        @DisplayName("без JWT → 401")
        void unauthenticated_returns401() {
            given()
                .when().get(BASE_PATH + "/" + BOOKING_ID)
                .then()
                .statusCode(401);
        }
    }
}
