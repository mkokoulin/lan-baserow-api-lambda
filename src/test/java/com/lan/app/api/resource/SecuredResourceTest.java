package com.lan.app.api.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class SecuredResourceTest {

    @Nested
    @DisplayName("GET /secured/public")
    class PublicEndpoint {

        @Test
        @DisplayName("без авторизации → 200 'public ok'")
        void unauthenticated_returns200() {
            given()
                .when().get("/secured/public")
                .then()
                .statusCode(200)
                .body(equalTo("public ok"));
        }
    }

    @Nested
    @DisplayName("GET /secured/user")
    class UserEndpoint {

        @Test
        @DisplayName("без авторизации → 401")
        void unauthenticated_returns401() {
            given()
                .when().get("/secured/user")
                .then()
                .statusCode(401);
        }

        @Test
        @DisplayName("с ролью web-users → 200 с именем и группами")
        @TestSecurity(user = "ivan.petrov", roles = {"web-users"})
        @JwtSecurity
        void authenticatedWebUser_returns200() {
            given()
                .when().get("/secured/user")
                .then()
                .statusCode(200)
                .body(containsString("ivan.petrov"))
                .body(containsString("web-users"));
        }

        @Test
        @DisplayName("с ролью admin → 200")
        @TestSecurity(user = "admin.user", roles = {"admin"})
        @JwtSecurity
        void authenticatedAdmin_returns200() {
            given()
                .when().get("/secured/user")
                .then()
                .statusCode(200)
                .body(containsString("admin.user"));
        }

        @Test
        @DisplayName("с ролью без доступа → 403")
        @TestSecurity(user = "guest.user", roles = {"guest"})
        void wrongRole_returns403() {
            given()
                .when().get("/secured/user")
                .then()
                .statusCode(403);
        }
    }
}
