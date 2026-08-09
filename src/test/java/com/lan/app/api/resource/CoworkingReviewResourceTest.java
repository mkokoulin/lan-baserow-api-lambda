package com.lan.app.api.resource;

import com.lan.app.domain.model.Review;
import com.lan.app.service.ReviewService;
import com.lan.app.service.command.CreateReviewCommand;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@QuarkusTest
class CoworkingReviewResourceTest {

    static final String BASE_PATH = "/coworking/v1/reviews";

    @InjectMock
    ReviewService service;

    static Review review() {
        return new Review(UUID.fromString("ffffffff-0000-0000-0000-000000000001"),
            "Ivan Petrov", 5, "Great place!", "2025-06-01T12:00:00Z");
    }

    @Nested
    @DisplayName("GET /coworking/v1/reviews")
    class List_ {

        @Test
        @DisplayName("без авторизации → 200 (эндпоинт публичный)")
        void unauthenticated_returns200() {
            when(service.list()).thenReturn(List.of(review()));

            given()
                .when().get(BASE_PATH)
                .then()
                .statusCode(200)
                .body("$",             hasSize(1))
                .body("[0].authorName", equalTo("Ivan Petrov"));
        }

        @Test
        @DisplayName("нет отзывов → 200 пустой массив")
        void noReviews_returnsEmptyList() {
            when(service.list()).thenReturn(List.of());

            given()
                .when().get(BASE_PATH)
                .then()
                .statusCode(200)
                .body("$", hasSize(0));
        }
    }

    @Nested
    @DisplayName("POST /coworking/v1/reviews")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class Create {

        @Test
        @DisplayName("валидный запрос → 201")
        void validRequest_returns201() {
            when(service.create(org.mockito.ArgumentMatchers.any())).thenReturn(review());

            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "authorName": "Ivan Petrov",
                        "rating": 5,
                        "text": "Great place!"
                    }
                """)
                .when().post(BASE_PATH)
                .then()
                .statusCode(201)
                .header("Location", containsString(review().id().toString()))
                .body("authorName", equalTo("Ivan Petrov"))
                .body("rating",     equalTo(5));
        }

        @Test
        @DisplayName("с eventRowId/guestRowId/registrationRowId → доходят до CreateReviewCommand")
        void withEventLinks_passesThroughToCommand() {
            when(service.create(org.mockito.ArgumentMatchers.any())).thenReturn(review());

            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "authorName": "Ivan Petrov",
                        "rating": 5,
                        "text": "Great event!",
                        "eventRowId": 42,
                        "guestRowId": 101,
                        "registrationRowId": 7
                    }
                """)
                .when().post(BASE_PATH)
                .then()
                .statusCode(201);

            var captor = org.mockito.ArgumentCaptor.forClass(CreateReviewCommand.class);
            org.mockito.Mockito.verify(service).create(captor.capture());
            assertEquals(42, captor.getValue().eventRowId());
            assertEquals(101, captor.getValue().guestRowId());
            assertEquals(7, captor.getValue().registrationRowId());
        }

        @Test
        @DisplayName("без event-полей (обычный отзыв) → CreateReviewCommand с null-ссылками")
        void withoutEventLinks_commandHasNullLinks() {
            when(service.create(org.mockito.ArgumentMatchers.any())).thenReturn(review());

            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "authorName": "Ivan Petrov",
                        "rating": 5,
                        "text": "Great place!"
                    }
                """)
                .when().post(BASE_PATH)
                .then()
                .statusCode(201);

            var captor = org.mockito.ArgumentCaptor.forClass(CreateReviewCommand.class);
            org.mockito.Mockito.verify(service).create(captor.capture());
            assertNull(captor.getValue().eventRowId());
            assertNull(captor.getValue().guestRowId());
            assertNull(captor.getValue().registrationRowId());
        }

        @Test
        @DisplayName("rating вне диапазона 1..5 → 400")
        void invalidRating_returns400() {
            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "authorName": "Ivan Petrov",
                        "rating": 6
                    }
                """)
                .when().post(BASE_PATH)
                .then()
                .statusCode(400);

            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("отсутствует authorName → 400")
        void missingAuthorName_returns400() {
            given()
                .contentType(ContentType.JSON)
                .body("""
                    { "rating": 5 }
                """)
                .when().post(BASE_PATH)
                .then()
                .statusCode(400);

            verifyNoInteractions(service);
        }
    }

    @Nested
    @DisplayName("POST /coworking/v1/reviews — без авторизации")
    class CreateUnauthenticated {

        @Test
        @DisplayName("без JWT → 401")
        void unauthenticated_returns401() {
            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "authorName": "Ivan Petrov",
                        "rating": 5
                    }
                """)
                .when().post(BASE_PATH)
                .then()
                .statusCode(401);
        }
    }
}
