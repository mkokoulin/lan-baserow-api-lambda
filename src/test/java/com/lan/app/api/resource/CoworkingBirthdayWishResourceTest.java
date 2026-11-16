package com.lan.app.api.resource;

import com.lan.app.domain.model.BirthdayWish;
import com.lan.app.service.BirthdayWishService;
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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@QuarkusTest
class CoworkingBirthdayWishResourceTest {

    static final String BASE_PATH = "/coworking/v1/birthday-wishes";

    @InjectMock
    BirthdayWishService service;

    static BirthdayWish wish() {
        return new BirthdayWish(UUID.fromString("eeeeeeee-0000-0000-0000-000000000001"),
            "Ivan Petrov", "123456789", "Happy birthday LAN!", "2026-11-16T12:00:00Z");
    }

    @Nested
    @DisplayName("GET /coworking/v1/birthday-wishes")
    class List_ {

        @Test
        @DisplayName("без авторизации → 200 (эндпоинт публичный)")
        void unauthenticated_returns200() {
            when(service.list()).thenReturn(List.of(wish()));

            given()
                .when().get(BASE_PATH)
                .then()
                .statusCode(200)
                .body("$",             hasSize(1))
                .body("[0].authorName", equalTo("Ivan Petrov"));
        }

        @Test
        @DisplayName("нет пожеланий → 200 пустой массив")
        void noWishes_returnsEmptyList() {
            when(service.list()).thenReturn(List.of());

            given()
                .when().get(BASE_PATH)
                .then()
                .statusCode(200)
                .body("$", hasSize(0));
        }
    }

    @Nested
    @DisplayName("POST /coworking/v1/birthday-wishes")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class Create {

        @Test
        @DisplayName("валидный запрос → 201")
        void validRequest_returns201() {
            when(service.create(org.mockito.ArgumentMatchers.any())).thenReturn(wish());

            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "authorName": "Ivan Petrov",
                        "telegramUserId": "123456789",
                        "text": "Happy birthday LAN!"
                    }
                """)
                .when().post(BASE_PATH)
                .then()
                .statusCode(201)
                .header("Location", containsString(wish().id().toString()))
                .body("authorName", equalTo("Ivan Petrov"))
                .body("text",       equalTo("Happy birthday LAN!"));
        }

        @Test
        @DisplayName("отсутствует text → 400")
        void missingText_returns400() {
            given()
                .contentType(ContentType.JSON)
                .body("""
                    { "authorName": "Ivan Petrov" }
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
                    { "text": "Happy birthday LAN!" }
                """)
                .when().post(BASE_PATH)
                .then()
                .statusCode(400);

            verifyNoInteractions(service);
        }
    }

    @Nested
    @DisplayName("POST /coworking/v1/birthday-wishes — без авторизации")
    class CreateUnauthenticated {

        @Test
        @DisplayName("без JWT → 401")
        void unauthenticated_returns401() {
            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "authorName": "Ivan Petrov",
                        "text": "Happy birthday LAN!"
                    }
                """)
                .when().post(BASE_PATH)
                .then()
                .statusCode(401);
        }
    }
}
