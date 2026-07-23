package com.lan.app.api.resource;

import com.lan.app.domain.model.SupportComplaint;
import com.lan.app.service.SupportComplaintService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@QuarkusTest
class SupportComplaintResourceTest {

    static final String BASE_PATH = "/support/v1/complaints";

    @InjectMock
    SupportComplaintService service;

    static SupportComplaint complaint() {
        return new SupportComplaint("Ivan", "+79161234567", "ivan", "billing", null, "comment");
    }

    static String validBody() {
        return """
            {
                "name":    "Ivan",
                "phone":   "+79161234567",
                "telegram": "ivan",
                "topic":   "billing",
                "comment": "comment"
            }
        """;
    }

    @Nested
    @DisplayName("POST /support/v1/complaints")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class Create {

        @Test
        @DisplayName("валидный запрос → 201 со статусом 'Новая'")
        void validRequest_returns201() {
            when(service.create(any())).thenReturn(complaint());

            given()
                .contentType(ContentType.JSON)
                .body(validBody())
                .when().post(BASE_PATH)
                .then()
                .statusCode(201)
                .body("status", equalTo("Новая"));
        }

        @Test
        @DisplayName("отсутствует name → 400")
        void missingName_returns400() {
            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "phone": "+79161234567",
                        "topic": "billing"
                    }
                """)
                .when().post(BASE_PATH)
                .then()
                .statusCode(400);

            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("отсутствует topic → 400")
        void missingTopic_returns400() {
            given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "name":  "Ivan",
                        "phone": "+79161234567"
                    }
                """)
                .when().post(BASE_PATH)
                .then()
                .statusCode(400);

            verifyNoInteractions(service);
        }
    }

    @Nested
    @DisplayName("POST /support/v1/complaints — без авторизации")
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
