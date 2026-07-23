package com.lan.app.api.resource;

import com.lan.app.domain.model.CoworkingGuest;
import com.lan.app.infrastructure.security.jwt.GuestTokenService;
import com.lan.app.service.CoworkingGuestService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

@QuarkusTest
class AuthResourceTest {

    static final String BASE_PATH = "/auth/v1/login";

    @InjectMock
    CoworkingGuestService guestService;

    @InjectMock
    GuestTokenService tokenService;

    static final UUID GUEST_ID = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");
    static final Long CHAT_ID = 123456789L;
    static final String PHONE = "+79161234567";

    static CoworkingGuest guest(Long chatId) {
        return new CoworkingGuest(GUEST_ID, chatId, "Ivan", "Petrov", "ivan_petrov", PHONE);
    }

    @Nested
    @DisplayName("POST /auth/v1/login")
    class Login {

        @Test
        @DisplayName("телефон найден, chatId совпадает → 200 с токеном")
        void matchingChatId_returns200WithToken() {
            when(guestService.findByPhone(PHONE)).thenReturn(Optional.of(guest(CHAT_ID)));
            when(tokenService.generateToken(GUEST_ID)).thenReturn("stub-jwt-token");

            given()
                .contentType(ContentType.JSON)
                .body("""
                    { "phone": "%s", "chat_id": %d }
                """.formatted(PHONE, CHAT_ID))
                .when().post(BASE_PATH)
                .then()
                .statusCode(200)
                .body("token", equalTo("stub-jwt-token"));
        }

        @Test
        @DisplayName("телефон не найден → 404 GUEST_NOT_FOUND")
        void phoneNotFound_returns404() {
            when(guestService.findByPhone(PHONE)).thenReturn(Optional.empty());

            given()
                .contentType(ContentType.JSON)
                .body("""
                    { "phone": "%s", "chat_id": %d }
                """.formatted(PHONE, CHAT_ID))
                .when().post(BASE_PATH)
                .then()
                .statusCode(404)
                .body("code", equalTo("GUEST_NOT_FOUND"));
        }

        @Test
        @DisplayName("chatId не совпадает → 401 CHAT_ID_MISMATCH")
        void chatIdMismatch_returns401() {
            when(guestService.findByPhone(PHONE)).thenReturn(Optional.of(guest(999L)));

            given()
                .contentType(ContentType.JSON)
                .body("""
                    { "phone": "%s", "chat_id": %d }
                """.formatted(PHONE, CHAT_ID))
                .when().post(BASE_PATH)
                .then()
                .statusCode(401)
                .body("code", equalTo("CHAT_ID_MISMATCH"));
        }

        @Test
        @DisplayName("у гостя не привязан chatId → 401 CHAT_ID_MISMATCH")
        void guestHasNoChatId_returns401() {
            when(guestService.findByPhone(PHONE)).thenReturn(Optional.of(guest(null)));

            given()
                .contentType(ContentType.JSON)
                .body("""
                    { "phone": "%s", "chat_id": %d }
                """.formatted(PHONE, CHAT_ID))
                .when().post(BASE_PATH)
                .then()
                .statusCode(401)
                .body("code", equalTo("CHAT_ID_MISMATCH"));
        }

        @Test
        @DisplayName("отсутствует phone → 400")
        void missingPhone_returns400() {
            given()
                .contentType(ContentType.JSON)
                .body("""
                    { "chat_id": %d }
                """.formatted(CHAT_ID))
                .when().post(BASE_PATH)
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("отсутствует chat_id → 400")
        void missingChatId_returns400() {
            given()
                .contentType(ContentType.JSON)
                .body("""
                    { "phone": "%s" }
                """.formatted(PHONE))
                .when().post(BASE_PATH)
                .then()
                .statusCode(400);
        }
    }
}
