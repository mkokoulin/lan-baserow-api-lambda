package com.lan.app.infrastructure.baserow.repository;

import com.baserow.dto.BaserowListResponse;
import com.lan.app.infrastructure.baserow.client.BaserowGuestClient;
import com.lan.app.infrastructure.baserow.dto.BaserowGuestRow;
import com.lan.app.infrastructure.baserow.dto.UpdateDigestSubscriptionRequest;
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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
@DisplayName("BaserowWeeklyDigestRepository")
class BaserowWeeklyDigestRepositoryTest {

    static final int TABLE_ID = 824729;

    @Inject
    BaserowWeeklyDigestRepository repo;

    @InjectMock
    @RestClient
    BaserowGuestClient client;

    static BaserowGuestRow guest(int rowId, Long chatId, Boolean digestSubscribed) {
        return new BaserowGuestRow(
            rowId, UUID.randomUUID(), "Guest", String.valueOf(rowId), "+374",
            "guest" + rowId, chatId, null, digestSubscribed, null, null, null, null
        );
    }

    static <T> BaserowListResponse<T> listOf(T... items) {
        return new BaserowListResponse<>(items.length, null, null, List.of(items));
    }

    @Nested
    @DisplayName("findSubscribers")
    class FindSubscribers {

        @Test
        @DisplayName("digest_subscribed = true и есть chatId → включён")
        void subscribedWithChatId_included() {
            when(client.listAllRaw(TABLE_ID)).thenReturn(listOf(guest(1, 111L, true)));

            var result = repo.findSubscribers();

            assertThat(result, contains(new com.lan.app.domain.model.DigestSubscriber(1, 111L)));
        }

        @Test
        @DisplayName("digest_subscribed = null и есть chatId → включён (opt-out по умолчанию)")
        void nullSubscriptionFlag_treatedAsSubscribed() {
            when(client.listAllRaw(TABLE_ID)).thenReturn(listOf(guest(2, 222L, null)));

            var result = repo.findSubscribers();

            assertThat(result, contains(new com.lan.app.domain.model.DigestSubscriber(2, 222L)));
        }

        @Test
        @DisplayName("digest_subscribed = false → исключён")
        void explicitlyUnsubscribed_excluded() {
            when(client.listAllRaw(TABLE_ID)).thenReturn(listOf(guest(3, 333L, false)));

            var result = repo.findSubscribers();

            assertThat(result, empty());
        }

        @Test
        @DisplayName("нет telegramChatId → исключён, даже если подписан")
        void noChatId_excluded() {
            when(client.listAllRaw(TABLE_ID)).thenReturn(listOf(guest(4, null, true)));

            var result = repo.findSubscribers();

            assertThat(result, empty());
        }
    }

    @Nested
    @DisplayName("unsubscribe")
    class Unsubscribe {

        @Test
        @DisplayName("патчит digest_subscribed = false для указанной строки")
        void patchesDigestSubscribedFalse() {
            repo.unsubscribe(661);

            verify(client).patchDigestSubscribed(TABLE_ID, 661, new UpdateDigestSubscriptionRequest(false));
        }
    }
}
