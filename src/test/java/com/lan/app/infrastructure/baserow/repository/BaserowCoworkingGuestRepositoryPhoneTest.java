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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("BaserowCoworkingGuestRepository — поиск по телефону")
class BaserowCoworkingGuestRepositoryPhoneTest {

    static final int    TABLE_ID       = 824729;
    static final Long   CHAT_ID        = 555000111L;
    static final String PHONE_E164     = "+37491083182";
    static final String PHONE_ENCODED  = "%2B37491083182";
    static final String PHONE_DIGITS   = "37491083182";
    static final String PHONE_LOCAL    = "91083182";

    @Inject
    BaserowCoworkingGuestRepository repo;

    @InjectMock
    @RestClient
    BaserowGuestClient client;

    static BaserowGuestRow row(String phone) {
        return new BaserowGuestRow(
            661,
            UUID.fromString("ca80b87b-b328-422f-8099-723a6ad79ebf"),
            "Misha", "K", phone, "sprmk", 299019145L, null
        );
    }

    static <T> BaserowListResponse<T> empty() {
        return new BaserowListResponse<>(0, null, null, List.of());
    }

    static <T> BaserowListResponse<T> of(T item) {
        return new BaserowListResponse<>(1, null, null, List.of(item));
    }

    // ─────────────────────────────────────────────────────────────────────
    // findByPhone
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findByPhone")
    class FindByPhone {

        @Test
        @DisplayName("ввод +374... → первый вариант %2B374... находит запись")
        void withPlusPrefix_foundOnEncodedVariant() {
            when(client.findByPhoneRaw(TABLE_ID, PHONE_ENCODED)).thenReturn(of(row(PHONE_E164)));
            when(client.findByPhoneRaw(TABLE_ID, PHONE_DIGITS)).thenReturn(empty());
            when(client.findByPhoneRaw(TABLE_ID, PHONE_LOCAL)).thenReturn(empty());

            var result = repo.findByPhone(PHONE_E164);

            assertTrue(result.isPresent());
            assertThat(result.get().phone(), equalTo(PHONE_E164));
            verify(client).findByPhoneRaw(TABLE_ID, PHONE_ENCODED);
            verify(client, never()).findByPhoneRaw(TABLE_ID, PHONE_DIGITS);
        }

        @Test
        @DisplayName("ввод 374... (без плюса) → те же варианты, находит через %2B")
        void withoutPlusPrefix_sameVariants() {
            when(client.findByPhoneRaw(TABLE_ID, PHONE_ENCODED)).thenReturn(of(row(PHONE_E164)));
            when(client.findByPhoneRaw(TABLE_ID, PHONE_DIGITS)).thenReturn(empty());
            when(client.findByPhoneRaw(TABLE_ID, PHONE_LOCAL)).thenReturn(empty());

            var result = repo.findByPhone(PHONE_DIGITS);

            assertTrue(result.isPresent());
            verify(client).findByPhoneRaw(TABLE_ID, PHONE_ENCODED);
        }

        @Test
        @DisplayName("ввод 91083182 (локальный) → те же варианты, находит через %2B")
        void localFormat_sameVariants() {
            when(client.findByPhoneRaw(TABLE_ID, PHONE_ENCODED)).thenReturn(of(row(PHONE_E164)));
            when(client.findByPhoneRaw(TABLE_ID, PHONE_DIGITS)).thenReturn(empty());
            when(client.findByPhoneRaw(TABLE_ID, PHONE_LOCAL)).thenReturn(empty());

            var result = repo.findByPhone(PHONE_LOCAL);

            assertTrue(result.isPresent());
            verify(client).findByPhoneRaw(TABLE_ID, PHONE_ENCODED);
        }

        @Test
        @DisplayName("%2B не найден → падает на цифровой вариант 374...")
        void fallsThrough_toDigitsVariant() {
            when(client.findByPhoneRaw(TABLE_ID, PHONE_ENCODED)).thenReturn(empty());
            when(client.findByPhoneRaw(TABLE_ID, PHONE_DIGITS)).thenReturn(of(row(PHONE_DIGITS)));
            when(client.findByPhoneRaw(TABLE_ID, PHONE_LOCAL)).thenReturn(empty());

            var result = repo.findByPhone(PHONE_E164);

            assertTrue(result.isPresent());
            verify(client).findByPhoneRaw(TABLE_ID, PHONE_ENCODED);
            verify(client).findByPhoneRaw(TABLE_ID, PHONE_DIGITS);
            verify(client, never()).findByPhoneRaw(TABLE_ID, PHONE_LOCAL);
        }

        @Test
        @DisplayName("первые два не найдены → падает на локальный формат 91...")
        void fallsThrough_toLocalVariant() {
            when(client.findByPhoneRaw(TABLE_ID, PHONE_ENCODED)).thenReturn(empty());
            when(client.findByPhoneRaw(TABLE_ID, PHONE_DIGITS)).thenReturn(empty());
            when(client.findByPhoneRaw(TABLE_ID, PHONE_LOCAL)).thenReturn(of(row(PHONE_LOCAL)));

            var result = repo.findByPhone(PHONE_E164);

            assertTrue(result.isPresent());
            verify(client).findByPhoneRaw(TABLE_ID, PHONE_ENCODED);
            verify(client).findByPhoneRaw(TABLE_ID, PHONE_DIGITS);
            verify(client).findByPhoneRaw(TABLE_ID, PHONE_LOCAL);
        }

        @Test
        @DisplayName("все варианты пустые → empty")
        void allVariantsEmpty_returnsEmpty() {
            when(client.findByPhoneRaw(eq(TABLE_ID), anyString())).thenReturn(empty());

            var result = repo.findByPhone(PHONE_E164);

            assertFalse(result.isPresent());
            verify(client, times(3)).findByPhoneRaw(eq(TABLE_ID), anyString());
        }

        @Test
        @DisplayName("null → empty, клиент не вызывается")
        void nullPhone_returnsEmpty() {
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
        @DisplayName("телефон найден через %2B → патч chatId, возвращает гостя")
        void found_patchesAndReturnsGuest() {
            var found = row(PHONE_E164);
            when(client.findByPhoneRaw(TABLE_ID, PHONE_ENCODED)).thenReturn(of(found));
            when(client.findByPhoneRaw(TABLE_ID, PHONE_DIGITS)).thenReturn(empty());
            when(client.findByPhoneRaw(TABLE_ID, PHONE_LOCAL)).thenReturn(empty());
            when(client.patchChatId(TABLE_ID, 661, new LinkChatIdRowRequest(CHAT_ID)))
                .thenReturn(new BaserowGuestRow(661, found.externalId(),
                    found.firstName(), found.lastName(), found.phone(),
                    found.telegram(), CHAT_ID, null));

            var result = repo.linkChatIdByPhone(PHONE_E164, CHAT_ID);

            assertTrue(result.isPresent());
            assertThat(result.get().telegramChatId(), is(CHAT_ID));
            verify(client).patchChatId(TABLE_ID, 661, new LinkChatIdRowRequest(CHAT_ID));
        }

        @Test
        @DisplayName("телефон не найден ни в одном варианте → empty, патч не вызывается")
        void notFound_returnsEmpty() {
            when(client.findByPhoneRaw(eq(TABLE_ID), anyString())).thenReturn(empty());

            var result = repo.linkChatIdByPhone(PHONE_E164, CHAT_ID);

            assertFalse(result.isPresent());
            verify(client, never()).patchChatId(anyInt(), anyInt(), any());
        }
    }
}
