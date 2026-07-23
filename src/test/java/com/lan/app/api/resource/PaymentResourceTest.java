package com.lan.app.api.resource;

import com.lan.app.repository.PaymentRepository;
import com.lan.app.service.PaymentService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
class PaymentResourceTest {

    static final String BASE_PATH = "/events/v1/payments";

    @InjectMock
    PaymentService service;

    @Nested
    @DisplayName("POST /events/v1/payments")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class Create {

        @Test
        @DisplayName("валидная multipart-форма без файла → 200 с paymentId")
        void withoutFile_returns200() {
            UUID paymentId = UUID.randomUUID();
            when(service.createPayment(any(), any(), any(), any(), any(), any(), any())).thenReturn(paymentId);

            given()
                .contentType(ContentType.MULTIPART)
                .multiPart("regId", "reg-1")
                .multiPart("guestName", "Ivan")
                .multiPart("phone", "+79161234567")
                .multiPart("eventName", "Event")
                .multiPart("amount", "1500.50")
                .when().post(BASE_PATH)
                .then()
                .statusCode(200)
                .body("paymentId", equalTo(paymentId.toString()));
        }

        @Test
        @DisplayName("некорректный amount → игнорируется, передаётся null")
        void invalidAmount_ignoredAsNull() {
            UUID paymentId = UUID.randomUUID();
            when(service.createPayment(any(), any(), any(), any(), org.mockito.ArgumentMatchers.isNull(), any(), any()))
                .thenReturn(paymentId);

            given()
                .contentType(ContentType.MULTIPART)
                .multiPart("regId", "reg-1")
                .multiPart("amount", "not-a-number")
                .when().post(BASE_PATH)
                .then()
                .statusCode(200)
                .body("paymentId", equalTo(paymentId.toString()));
        }
    }

    @Nested
    @DisplayName("POST /events/v1/payments — без авторизации")
    class CreateUnauthenticated {

        @Test
        @DisplayName("без JWT → 401")
        void unauthenticated_returns401() {
            given()
                .contentType(ContentType.MULTIPART)
                .multiPart("regId", "reg-1")
                .when().post(BASE_PATH)
                .then()
                .statusCode(401);
        }
    }

    @Nested
    @DisplayName("POST /{paymentId}/approve")
    class Approve {

        @Test
        @DisplayName("некорректный paymentId → 400")
        void invalidPaymentId_returns400() {
            given()
                .when().post(BASE_PATH + "/not-a-uuid/approve")
                .then()
                .statusCode(400)
                .body("error", equalTo("invalid paymentId"));
        }

        @Test
        @DisplayName("платёж не найден → 404")
        void notFound_returns404() {
            UUID paymentId = UUID.randomUUID();
            when(service.approve(paymentId)).thenReturn(new PaymentRepository.ApproveResult(Optional.empty(), null));

            given()
                .when().post(BASE_PATH + "/" + paymentId + "/approve")
                .then()
                .statusCode(404)
                .body("error", equalTo("payment not found"));
        }

        @Test
        @DisplayName("chatId неизвестен → 200 chatId=null (не 500)")
        void noChatId_returns200WithNullChatId() {
            UUID paymentId = UUID.randomUUID();
            when(service.approve(paymentId))
                .thenReturn(new PaymentRepository.ApproveResult(Optional.empty(), "reg-1"));

            given()
                .when().post(BASE_PATH + "/" + paymentId + "/approve")
                .then()
                .statusCode(200)
                .body("chatId", nullValue());
        }

        @Test
        @DisplayName("chatId известен → 200 с chatId")
        void withChatId_returns200() {
            UUID paymentId = UUID.randomUUID();
            when(service.approve(paymentId))
                .thenReturn(new PaymentRepository.ApproveResult(Optional.of(555L), "reg-1"));

            given()
                .when().post(BASE_PATH + "/" + paymentId + "/approve")
                .then()
                .statusCode(200)
                .body("chatId", equalTo(555));
        }
    }

    @Nested
    @DisplayName("POST /{paymentId}/reject")
    class Reject {

        @Test
        @DisplayName("некорректный paymentId → 400")
        void invalidPaymentId_returns400() {
            given()
                .when().post(BASE_PATH + "/not-a-uuid/reject")
                .then()
                .statusCode(400)
                .body("error", equalTo("invalid paymentId"));
        }

        @Test
        @DisplayName("платёж не найден → 404")
        void notFound_returns404() {
            UUID paymentId = UUID.randomUUID();
            when(service.reject(paymentId)).thenReturn(new PaymentRepository.RejectResult(false, Optional.empty()));

            given()
                .when().post(BASE_PATH + "/" + paymentId + "/reject")
                .then()
                .statusCode(404)
                .body("error", equalTo("payment not found"));
        }

        @Test
        @DisplayName("chatId неизвестен → 200 chatId=null (не 500)")
        void noChatId_returns200WithNullChatId() {
            UUID paymentId = UUID.randomUUID();
            when(service.reject(paymentId)).thenReturn(new PaymentRepository.RejectResult(true, Optional.empty()));

            given()
                .when().post(BASE_PATH + "/" + paymentId + "/reject")
                .then()
                .statusCode(200)
                .body("chatId", nullValue());
        }

        @Test
        @DisplayName("chatId известен → 200 с chatId")
        void withChatId_returns200() {
            UUID paymentId = UUID.randomUUID();
            when(service.reject(paymentId)).thenReturn(new PaymentRepository.RejectResult(true, Optional.of(777L)));

            given()
                .when().post(BASE_PATH + "/" + paymentId + "/reject")
                .then()
                .statusCode(200)
                .body("chatId", equalTo(777));
        }
    }
}
