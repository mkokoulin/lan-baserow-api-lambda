package com.lan.app.api.resource;

import com.lan.app.domain.exception.ResourceNotFoundException;
import com.lan.app.domain.model.Id;
import com.lan.app.domain.model.Vacancy;
import com.lan.app.service.VacancyService;
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
class VacancyResourceTest {

    static final String BASE_PATH = "/careers/v1/vacancies";
    static final UUID VACANCY_ID = UUID.fromString("11111111-2222-0000-0000-000000000001");

    @InjectMock
    VacancyService service;

    static Vacancy vacancy() {
        return new Vacancy(new Id(1, VACANCY_ID), "Бармен", "Актуально до 20 сентября", "desc", "http://apply");
    }

    @Nested
    @DisplayName("GET /careers/v1/vacancies")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class List_ {

        @Test
        @DisplayName("есть вакансии → 200 массив")
        void withVacancies_returnsList() {
            when(service.list()).thenReturn(List.of(vacancy()));

            given()
                .when().get(BASE_PATH)
                .then()
                .statusCode(200)
                .body("$",        hasSize(1))
                .body("[0].title", equalTo("Бармен"));
        }

        @Test
        @DisplayName("нет вакансий → 200 пустой массив")
        void noVacancies_returnsEmptyList() {
            when(service.list()).thenReturn(List.of());

            given()
                .when().get(BASE_PATH)
                .then()
                .statusCode(200)
                .body("$", hasSize(0));
        }
    }

    @Nested
    @DisplayName("GET /careers/v1/vacancies/{externalId}")
    @TestSecurity(user = "test-user", roles = {"web-users"})
    class GetById {

        @Test
        @DisplayName("вакансия найдена → 200")
        void found_returns200() {
            when(service.get(VACANCY_ID)).thenReturn(vacancy());

            given()
                .when().get(BASE_PATH + "/" + VACANCY_ID)
                .then()
                .statusCode(200)
                .body("id",    equalTo(VACANCY_ID.toString()))
                .body("title", equalTo("Бармен"));
        }

        @Test
        @DisplayName("вакансия не найдена → 404 RESOURCE_NOT_FOUND")
        void notFound_returns404() {
            when(service.get(VACANCY_ID)).thenThrow(new ResourceNotFoundException("Vacancy", VACANCY_ID));

            given()
                .when().get(BASE_PATH + "/" + VACANCY_ID)
                .then()
                .statusCode(404)
                .body("code", equalTo("RESOURCE_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("GET /careers/v1/vacancies — без авторизации")
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
