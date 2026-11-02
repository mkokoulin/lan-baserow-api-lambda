package com.lan.app.infrastructure.baserow.repository;

import com.baserow.dto.BaserowListResponse;
import com.lan.app.infrastructure.baserow.client.BaserowGuestClient;
import com.lan.app.infrastructure.baserow.dto.BaserowGuestRow;
import com.lan.app.infrastructure.baserow.dto.LinkChatIdRowRequest;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@QuarkusTest
@DisplayName("BaserowCoworkingGuestRepository — поиск по телефону")
class BaserowCoworkingGuestRepositoryPhoneTest {

    static final int  TABLE_ID = 824729;
    static final Long CHAT_ID  = 555000111L;

    @Inject
    BaserowCoworkingGuestRepository repo;

    @InjectMock
    @RestClient
    BaserowGuestClient client;

    static BaserowGuestRow row(int id, String phone) {
        return new BaserowGuestRow(
            id, UUID.fromString("ca80b87b-b328-422f-8099-723a6ad79ebf"),
            "Misha", "K", phone, "sprmk", 299019145L, null, null, null, null, null, null
        );
    }

    static <T> BaserowListResponse<T> listOf(T... items) {
        return new BaserowListResponse<>(items.length, null, null, List.of(items));
    }

    // ─────────────────────────────────────────────────────────────────────
    // findByPhone
    // ─────────────────────────────────────────────────────────────────────

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
            verifyNoInteractions(client);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // linkChatIdByPhone
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("linkChatIdByPhone")
    class LinkChatIdByPhone {

        @Test
        @DisplayName("телефон хранится с пробелами, ищут цифрами → находит, патчит chatId")
        void foundDespiteFormatting_patchesAndReturnsGuest() {
            var found = row(661, "+374 91 083 182");
            when(client.listAllRaw(TABLE_ID)).thenReturn(listOf(found));
            when(client.patchChatId(TABLE_ID, 661, new LinkChatIdRowRequest(CHAT_ID)))
                .thenReturn(new BaserowGuestRow(661, found.externalId(),
                    found.firstName(), found.lastName(), found.phone(),
                    found.telegram(), CHAT_ID, null, null, null, null, null, null));

            var result = repo.linkChatIdByPhone("+37491083182", CHAT_ID);

            assertTrue(result.isPresent());
            assertThat(result.get().telegramChatId(), is(CHAT_ID));
            verify(client).patchChatId(TABLE_ID, 661, new LinkChatIdRowRequest(CHAT_ID));
        }

        @Test
        @DisplayName("телефон не найден → empty, патч не вызывается")
        void notFound_returnsEmpty() {
            when(client.listAllRaw(TABLE_ID)).thenReturn(listOf(row(661, "+374 99 111 222")));

            var result = repo.linkChatIdByPhone("+37491083182", CHAT_ID);

            assertFalse(result.isPresent());
            verify(client, never()).patchChatId(anyInt(), anyInt(), any());
        }

        @Test
        @DisplayName("null → empty, клиент не вызывается")
        void nullPhone_returnsEmpty() {
            var result = repo.linkChatIdByPhone(null, CHAT_ID);

            assertFalse(result.isPresent());
            verifyNoInteractions(client);
        }
    }
}
