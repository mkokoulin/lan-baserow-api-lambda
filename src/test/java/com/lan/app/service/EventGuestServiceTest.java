package com.lan.app.service;

import com.lan.app.domain.model.EventGuest;
import com.lan.app.domain.model.Id;
import com.lan.app.repository.EventGuestRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventGuestService")
class EventGuestServiceTest {

    @Mock
    EventGuestRepository repo;

    EventGuestService service;

    static EventGuest guest() {
        return new EventGuest(new Id(1, UUID.randomUUID()), "Ivan", "Petrov", "ivan", "+7900", "telegram-bot", 555L);
    }

    @Nested
    class Create {

        @Test
        @DisplayName("chatId уже привязан к существующему гостю → возвращает его, новую строку не создаёт")
        void existingChatId_reusesExistingGuest() {
            service = new EventGuestService(repo);
            var existing = guest();
            when(repo.findByTelegramChatId(555L)).thenReturn(Optional.of(existing));

            var result = service.create("Ivan", "Petrov", "+7900", "ivan", "telegram-bot", 555L);

            assertEquals(existing, result);
            verify(repo, never()).create(any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("chatId не найден → создаёт нового гостя")
        void newChatId_createsGuest() {
            service = new EventGuestService(repo);
            when(repo.findByTelegramChatId(555L)).thenReturn(Optional.empty());
            var created = guest();
            when(repo.create("Ivan", "Petrov", "+7900", "ivan", "telegram-bot", 555L)).thenReturn(created);

            var result = service.create("Ivan", "Petrov", "+7900", "ivan", "telegram-bot", 555L);

            assertEquals(created, result);
        }

        @Test
        @DisplayName("chatId отсутствует (null) → создаёт нового гостя без поиска")
        void nullChatId_createsGuestWithoutLookup() {
            service = new EventGuestService(repo);
            var created = guest();
            when(repo.create("Ivan", "Petrov", "+7900", "ivan", "telegram-bot", null)).thenReturn(created);

            var result = service.create("Ivan", "Petrov", "+7900", "ivan", "telegram-bot", null);

            assertEquals(created, result);
            verify(repo, never()).findByTelegramChatId(any());
        }
    }
}
