package com.lan.app.api.resource;

import com.lan.app.domain.model.CoworkingNew;
import com.lan.app.service.CoworkingNewService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;

@QuarkusTest
class CoworkingNewResourceTest {

    static final String BASE_PATH = "/coworking/v1/news";

    @InjectMock
    CoworkingNewService service;

    static CoworkingNew item() {
        return new CoworkingNew(UUID.fromString("dddddddd-0000-0000-0000-000000000001"),
            "Title EN", "Заголовок RU", "Body EN", "Тело RU", "http://img", "http://link");
    }

    @Nested
    @DisplayName("GET /coworking/v1/news")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class List_ {

        @Test
        @DisplayName("есть новости → 200 массив")
        void withNews_returnsList() {
            when(service.list()).thenReturn(List.of(item()));

            given()
                .when().get(BASE_PATH)
                .then()
                .statusCode(200)
                .body("$",           hasSize(1))
                .body("[0].titleRu", equalTo("Заголовок RU"));
        }

        @Test
        @DisplayName("нет новостей → 200 пустой массив")
        void noNews_returnsEmptyList() {
            when(service.list()).thenReturn(List.of());

            given()
                .when().get(BASE_PATH)
                .then()
                .statusCode(200)
                .body("$", hasSize(0));
        }
    }

    @Nested
    @DisplayName("GET /coworking/v1/news — без авторизации")
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
