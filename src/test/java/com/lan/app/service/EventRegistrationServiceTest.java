package com.lan.app.service;

import com.lan.app.domain.exception.BusinessConflictException;
import com.lan.app.domain.model.Event;
import com.lan.app.domain.model.EventGuest;
import com.lan.app.domain.model.EventRegistration;
import com.lan.app.domain.model.EventRegistrationItem;
import com.lan.app.domain.model.Id;
import com.lan.app.repository.EventGuestRepository;
import com.lan.app.repository.EventRegistrationRepository;
import com.lan.app.repository.EventRepository;
import com.lan.app.service.command.CreateEventRegistrationCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventRegistrationService")
class EventRegistrationServiceTest {

    @Mock
    EventRepository eventRepo;
    @Mock
    EventGuestRepository guestRepo;
    @Mock
    EventRegistrationRepository registrationRepo;
    @Mock
    EventCapacityService capacityService;

    EventRegistrationService service;

    static final UUID EVENT_EXTERNAL_ID = UUID.randomUUID();
    static final UUID GUEST_EXTERNAL_ID = UUID.randomUUID();
    static final Id EVENT_ID = new Id(1, EVENT_EXTERNAL_ID);
    static final Id GUEST_ID = new Id(2, GUEST_EXTERNAL_ID);

    static Event event(boolean soldOut) {
        return new Event(
            EVENT_ID, "Событие", Instant.now(), Instant.now(), "d",
            null, null, null, null, true, List.of(), null, null,
            true, true, false, BigDecimal.ZERO, null, 10, soldOut, null
        );
    }

    static EventGuest guest() {
        return new EventGuest(GUEST_ID, "Ivan", "Petrov", "ivan", "+7900", "web", null);
    }

    @Nested
    class Create {

        @Test
        @DisplayName("событие не sold out → регистрация создаётся")
        void notSoldOut_createsRegistration() {
            service = new EventRegistrationService(eventRepo, guestRepo, registrationRepo, capacityService);
            var cmd = new CreateEventRegistrationCommand(EVENT_EXTERNAL_ID, GUEST_EXTERNAL_ID, "comment", 2, "web");
            when(eventRepo.get(EVENT_EXTERNAL_ID)).thenReturn(event(false));
            when(capacityService.remainingCapacity(10, EVENT_ID.internalId())).thenReturn(5);
            when(guestRepo.get(GUEST_EXTERNAL_ID)).thenReturn(guest());
            var expected = new EventRegistration(new Id(3, UUID.randomUUID()), EVENT_ID, GUEST_ID, 2, "comment", "web", false);
            when(registrationRepo.create(EVENT_ID, GUEST_ID, 2, "comment", "web")).thenReturn(expected);

            var result = service.create(cmd);

            assertEquals(expected, result);
        }

        @Test
        @DisplayName("событие sold out → BusinessConflictException, регистрация не создаётся")
        void soldOut_throwsConflict() {
            service = new EventRegistrationService(eventRepo, guestRepo, registrationRepo, capacityService);
            var cmd = new CreateEventRegistrationCommand(EVENT_EXTERNAL_ID, GUEST_EXTERNAL_ID, "comment", 2, "web");
            when(eventRepo.get(EVENT_EXTERNAL_ID)).thenReturn(event(true));

            assertThrows(BusinessConflictException.class, () -> service.create(cmd));

            verifyNoInteractions(guestRepo);
            verify(registrationRepo, never()).create(any(), any(), anyInt(), any(), any());
        }

        @Test
        @DisplayName("guestCount превышает оставшиеся места → BusinessConflictException, регистрация не создаётся")
        void guestCountExceedsRemaining_throwsConflict() {
            service = new EventRegistrationService(eventRepo, guestRepo, registrationRepo, capacityService);
            var cmd = new CreateEventRegistrationCommand(EVENT_EXTERNAL_ID, GUEST_EXTERNAL_ID, "comment", 4, "web");
            when(eventRepo.get(EVENT_EXTERNAL_ID)).thenReturn(event(false));
            when(capacityService.remainingCapacity(10, EVENT_ID.internalId())).thenReturn(3);

            var ex = assertThrows(BusinessConflictException.class, () -> service.create(cmd));

            assertEquals(3, ex.details().get("availableSpots"));
            verifyNoInteractions(guestRepo);
            verify(registrationRepo, never()).create(any(), any(), anyInt(), any(), any());
        }
    }

    @Nested
    class FindByChatId {

        @Test
        @DisplayName("гость по chatId найден → возвращает его регистрации")
        void guestFound_returnsRegistrations() {
            service = new EventRegistrationService(eventRepo, guestRepo, registrationRepo, capacityService);
            Long chatId = 123L;
            when(guestRepo.findByTelegramChatId(chatId)).thenReturn(Optional.of(guest()));
            var item = new EventRegistrationItem("Событие", Instant.now());
            when(registrationRepo.findByGuestRowId(GUEST_ID.internalId())).thenReturn(List.of(item));

            var result = service.findByChatId(chatId);

            assertEquals(List.of(item), result);
        }

        @Test
        @DisplayName("гость по chatId не найден → пустой список")
        void guestNotFound_returnsEmptyList() {
            service = new EventRegistrationService(eventRepo, guestRepo, registrationRepo, capacityService);
            Long chatId = 123L;
            when(guestRepo.findByTelegramChatId(chatId)).thenReturn(Optional.empty());

            var result = service.findByChatId(chatId);

            assertTrue(result.isEmpty());
            verifyNoInteractions(registrationRepo);
        }
    }

    @Nested
    class StoreTelegramChatIdForGuest {

        @Test
        @DisplayName("repo кидает исключение → проглатывается, не пробрасывается наружу")
        void repoThrows_isSwallowed() {
            service = new EventRegistrationService(eventRepo, guestRepo, registrationRepo, capacityService);
            doThrow(new RuntimeException("boom")).when(guestRepo).storeTelegramChatId(1, 123L);

            assertDoesNotThrow(() -> service.storeTelegramChatIdForGuest(1, 123L));
        }
    }

    @Nested
    class StoreTelegramChatId {

        @Test
        @DisplayName("гость найден по регистрации → chatId сохраняется")
        void guestFound_storesChatId() {
            service = new EventRegistrationService(eventRepo, guestRepo, registrationRepo, capacityService);
            UUID regId = UUID.randomUUID();
            when(registrationRepo.getGuestRowIdByExternalId(regId)).thenReturn(Optional.of(7));

            service.storeTelegramChatId(regId, 999L);

            verify(guestRepo).storeTelegramChatId(7, 999L);
        }

        @Test
        @DisplayName("гость не найден по регистрации → chatId не сохраняется")
        void guestNotFound_doesNotStore() {
            service = new EventRegistrationService(eventRepo, guestRepo, registrationRepo, capacityService);
            UUID regId = UUID.randomUUID();
            when(registrationRepo.getGuestRowIdByExternalId(regId)).thenReturn(Optional.empty());

            service.storeTelegramChatId(regId, 999L);

            verify(guestRepo, never()).storeTelegramChatId(anyInt(), any());
        }

        @Test
        @DisplayName("repo кидает исключение → проглатывается, не пробрасывается наружу")
        void repoThrows_isSwallowed() {
            service = new EventRegistrationService(eventRepo, guestRepo, registrationRepo, capacityService);
            UUID regId = UUID.randomUUID();
            when(registrationRepo.getGuestRowIdByExternalId(regId)).thenThrow(new RuntimeException("boom"));

            assertDoesNotThrow(() -> service.storeTelegramChatId(regId, 999L));
        }
    }
}
