package com.lan.app.infrastructure.baserow.repository;

import com.baserow.dto.BaserowListResponse;
import com.lan.app.infrastructure.baserow.client.BaserowGuestClient;
import com.lan.app.infrastructure.baserow.dto.BaserowGuestRow;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@QuarkusTest
@DisplayName("BaserowEventGuestRepository — поиск по телефону")
class BaserowEventGuestRepositoryPhoneTest {

    static final int TABLE_ID = 824729;

    @Inject
    BaserowEventGuestRepository repo;

    @InjectMock
    @RestClient
    BaserowGuestClient client;

    static BaserowGuestRow row(int id, String phone) {
        return new BaserowGuestRow(id, UUID.randomUUID(), "Misha", "K", phone, "sprmk", null, null);
    }

    static <T> BaserowListResponse<T> listOf(T... items) {
        return new BaserowListResponse<>(items.length, null, null, List.of(items));
    }

    @Nested
    @DisplayName("findByPhone")
    class FindByPhone {

        @Test
        @DisplayName("сохранённый номер с пробелами внутри → всё равно находится по нормализованным цифрам")
        void storedWithInternalSpaces_stillMatches() {
            when(client.listAllRaw(TABLE_ID)).thenReturn(listOf(row(661, "+374 91 083 182")));

            var result = repo.findByPhone("+37491083182");

            assertTrue(result.isPresent());
        }

        @Test
        @DisplayName("искомый номер с пробелами, сохранённый — без → тоже находится")
        void queryWithSpaces_matchesCleanStoredValue() {
            when(client.listAllRaw(TABLE_ID)).thenReturn(listOf(row(661, "37491083182")));

            var result = repo.findByPhone("+374 91 083 182");

            assertTrue(result.isPresent());
        }

        @Test
        @DisplayName("локальный формат без кода страны совпадает с полным международным")
        void localFormat_matchesFullInternational() {
            when(client.listAllRaw(TABLE_ID)).thenReturn(listOf(row(661, "+374 91 083 182")));

            var result = repo.findByPhone("91083182");

            assertTrue(result.isPresent());
        }

        @Test
        @DisplayName("совпадений нет → empty")
        void noMatch_returnsEmpty() {
            when(client.listAllRaw(TABLE_ID)).thenReturn(listOf(row(661, "+374 99 111 222")));

            var result = repo.findByPhone("+374 91 083 182");

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("null → empty, клиент не вызывается")
        void nullPhone_returnsEmptyWithoutCallingClient() {
            var result = repo.findByPhone(null);

            assertFalse(result.isPresent());
        }
    }
}
