package com.lan.app.service;

import com.lan.app.domain.exception.BusinessConflictException;
import com.lan.app.domain.model.CoworkingGuest;
import com.lan.app.repository.CoworkingGuestRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CoworkingGuestService.create")
class CoworkingGuestServiceTest {

    @Mock
    CoworkingGuestRepository repo;

    CoworkingGuestService service;

    static final String PHONE = "+79161234567";
    static final Long CHAT_ID = 123L;
    static final UUID GUEST_ID = UUID.randomUUID();

    static CoworkingGuest guest() {
        return new CoworkingGuest(GUEST_ID, CHAT_ID, "Ivan", "Petrov", "ivan", PHONE);
    }

    @Nested
    class NoConflicts {

        @Test
        @DisplayName("телефон и chatId свободны → гость создаётся")
        void createsGuest() {
            service = new CoworkingGuestService(repo);
            when(repo.findByPhone(PHONE)).thenReturn(Optional.empty());
            when(repo.findByChatId(CHAT_ID)).thenReturn(Optional.empty());
            when(repo.create("Ivan", "Petrov", PHONE, "ivan", CHAT_ID)).thenReturn(guest());

            var result = service.create("Ivan", "Petrov", PHONE, "ivan", CHAT_ID);

            assertEquals(guest(), result);
        }

        @Test
        @DisplayName("telegramChatId == null → проверка по chatId не выполняется")
        void nullChatId_skipsChatIdCheck() {
            service = new CoworkingGuestService(repo);
            when(repo.findByPhone(PHONE)).thenReturn(Optional.empty());
            when(repo.create("Ivan", "Petrov", PHONE, "ivan", null)).thenReturn(guest());

            service.create("Ivan", "Petrov", PHONE, "ivan", null);

            verify(repo, never()).findByChatId(any());
        }
    }

    @Nested
    class PhoneConflict {

        @Test
        @DisplayName("телефон уже занят → BusinessConflictException, create не вызывается")
        void phoneTaken_throwsConflict() {
            service = new CoworkingGuestService(repo);
            when(repo.findByPhone(PHONE)).thenReturn(Optional.of(guest()));

            var ex = assertThrows(BusinessConflictException.class,
                () -> service.create("Ivan", "Petrov", PHONE, "ivan", CHAT_ID));

            assertTrue(ex.getMessage().contains(PHONE));
            verify(repo, never()).findByChatId(any());
            verify(repo, never()).create(any(), any(), any(), any(), any());
        }
    }

    @Nested
    class ChatIdConflict {

        @Test
        @DisplayName("chatId уже занят другим гостем → BusinessConflictException, create не вызывается")
        void chatIdTaken_throwsConflict() {
            service = new CoworkingGuestService(repo);
            when(repo.findByPhone(PHONE)).thenReturn(Optional.empty());
            when(repo.findByChatId(CHAT_ID)).thenReturn(Optional.of(guest()));

            assertThrows(BusinessConflictException.class,
                () -> service.create("Ivan", "Petrov", PHONE, "ivan", CHAT_ID));

            verify(repo, never()).create(any(), any(), any(), any(), any());
        }
    }
}
