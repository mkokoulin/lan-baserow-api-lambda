package com.lan.app.service;

import com.lan.app.domain.exception.BusinessConflictException;
import com.lan.app.domain.exception.RegistrationNotFoundException;
import com.lan.app.domain.exception.ValidationException;
import com.lan.app.domain.model.Event;
import com.lan.app.domain.model.EventGuest;
import com.lan.app.domain.model.EventRegistration;
import com.lan.app.domain.model.EventRegistrationItem;
import com.lan.app.domain.model.Id;
import com.lan.app.domain.model.RegistrationActionResult;
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
            var expected = new EventRegistration(new Id(3, UUID.randomUUID()), EVENT_ID, GUEST_ID, 2, "comment", "web", false, false);
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
            var item = new EventRegistrationItem(UUID.randomUUID(), "Событие", Instant.now(), 2, false);
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
        @DisplayName("гость с этим chatId ещё не существует → chatId сохраняется на переданный guestRowId")
        void noExistingGuest_storesChatIdOnGivenRow() {
            service = new EventRegistrationService(eventRepo, guestRepo, registrationRepo, capacityService);
            UUID regId = UUID.randomUUID();
            when(guestRepo.findByTelegramChatId(123L)).thenReturn(Optional.empty());

            service.storeTelegramChatIdForGuest(regId, 1, 123L);

            verify(guestRepo).storeTelegramChatId(1, 123L);
            verify(registrationRepo, never()).relinkGuest(any(), anyInt());
        }

        @Test
        @DisplayName("chatId уже привязан к ДРУГОЙ строке гостя → регистрация перелинковывается на существующего гостя, дубликат не трогается")
        void existingGuestOnDifferentRow_relinksRegistrationInstead() {
            service = new EventRegistrationService(eventRepo, guestRepo, registrationRepo, capacityService);
            UUID regId = UUID.randomUUID();
            var existingGuest = new EventGuest(new Id(99, UUID.randomUUID()), "Ann", "Smith", "ann", "+7900", "web", 123L);
            when(guestRepo.findByTelegramChatId(123L)).thenReturn(Optional.of(existingGuest));

            service.storeTelegramChatIdForGuest(regId, 1, 123L);

            verify(registrationRepo).relinkGuest(regId, 99);
            verify(guestRepo, never()).storeTelegramChatId(anyInt(), any());
        }

        @Test
        @DisplayName("chatId уже привязан к ТОЙ ЖЕ строке гостя → повторно ничего не делает")
        void existingGuestOnSameRow_doesNothingExtra() {
            service = new EventRegistrationService(eventRepo, guestRepo, registrationRepo, capacityService);
            UUID regId = UUID.randomUUID();
            var existingGuest = new EventGuest(new Id(1, UUID.randomUUID()), "Ann", "Smith", "ann", "+7900", "web", 123L);
            when(guestRepo.findByTelegramChatId(123L)).thenReturn(Optional.of(existingGuest));

            service.storeTelegramChatIdForGuest(regId, 1, 123L);

            verify(guestRepo).storeTelegramChatId(1, 123L);
            verify(registrationRepo, never()).relinkGuest(any(), anyInt());
        }

        @Test
        @DisplayName("repo кидает исключение → проглатывается, не пробрасывается наружу")
        void repoThrows_isSwallowed() {
            service = new EventRegistrationService(eventRepo, guestRepo, registrationRepo, capacityService);
            UUID regId = UUID.randomUUID();
            when(guestRepo.findByTelegramChatId(123L)).thenReturn(Optional.empty());
            doThrow(new RuntimeException("boom")).when(guestRepo).storeTelegramChatId(1, 123L);

            assertDoesNotThrow(() -> service.storeTelegramChatIdForGuest(regId, 1, 123L));
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

    @Nested
    class Cancel {

        static final UUID REG_ID = UUID.randomUUID();

        @Test
        @DisplayName("активная регистрация на будущее событие → отменяется")
        void active_getsCancelled() {
            service = new EventRegistrationService(eventRepo, guestRepo, registrationRepo, capacityService);
            var item = new EventRegistrationItem(REG_ID, "Событие", Instant.now().plusSeconds(3600), 2, false);
            var expected = new RegistrationActionResult("Событие", item.dateStart(), 2, 2, "Ivan", "Petrov", "+7900", "ivan");
            when(registrationRepo.findByExternalId(REG_ID)).thenReturn(Optional.of(item));
            when(registrationRepo.cancel(REG_ID)).thenReturn(Optional.of(expected));

            var result = service.cancel(REG_ID);

            assertEquals(expected, result);
        }

        @Test
        @DisplayName("регистрация не найдена → RegistrationNotFoundException")
        void notFound_throws() {
            service = new EventRegistrationService(eventRepo, guestRepo, registrationRepo, capacityService);
            when(registrationRepo.findByExternalId(REG_ID)).thenReturn(Optional.empty());

            assertThrows(RegistrationNotFoundException.class, () -> service.cancel(REG_ID));
            verify(registrationRepo, never()).cancel(any());
        }

        @Test
        @DisplayName("уже отменена → BusinessConflictException")
        void alreadyCancelled_throwsConflict() {
            service = new EventRegistrationService(eventRepo, guestRepo, registrationRepo, capacityService);
            var item = new EventRegistrationItem(REG_ID, "Событие", Instant.now().plusSeconds(3600), 2, true);
            when(registrationRepo.findByExternalId(REG_ID)).thenReturn(Optional.of(item));

            assertThrows(BusinessConflictException.class, () -> service.cancel(REG_ID));
            verify(registrationRepo, never()).cancel(any());
        }

        @Test
        @DisplayName("событие уже началось → BusinessConflictException")
        void eventStarted_throwsConflict() {
            service = new EventRegistrationService(eventRepo, guestRepo, registrationRepo, capacityService);
            var item = new EventRegistrationItem(REG_ID, "Событие", Instant.now().minusSeconds(3600), 2, false);
            when(registrationRepo.findByExternalId(REG_ID)).thenReturn(Optional.of(item));

            assertThrows(BusinessConflictException.class, () -> service.cancel(REG_ID));
            verify(registrationRepo, never()).cancel(any());
        }
    }

    @Nested
    class UpdateGuestCount {

        static final UUID REG_ID = UUID.randomUUID();

        @Test
        @DisplayName("достаточно мест → количество гостей обновляется")
        void enoughCapacity_updates() {
            service = new EventRegistrationService(eventRepo, guestRepo, registrationRepo, capacityService);
            var item = new EventRegistrationItem(REG_ID, "Событие", Instant.now().plusSeconds(3600), 2, false);
            var expected = new RegistrationActionResult("Событие", item.dateStart(), 2, 4, "Ivan", "Petrov", "+7900", "ivan");
            when(registrationRepo.findByExternalId(REG_ID)).thenReturn(Optional.of(item));
            when(registrationRepo.getEventRowIdByExternalId(REG_ID)).thenReturn(Optional.of(EVENT_ID.internalId()));
            when(eventRepo.getByRowId(EVENT_ID.internalId())).thenReturn(event(false));
            when(capacityService.registeredGuestCount(EVENT_ID.internalId())).thenReturn(6);
            when(registrationRepo.updateGuestCount(REG_ID, 4)).thenReturn(Optional.of(expected));

            var result = service.updateGuestCount(REG_ID, 4);

            assertEquals(expected, result);
        }

        @Test
        @DisplayName("не хватает мест → BusinessConflictException, изменение не применяется")
        void notEnoughCapacity_throwsConflict() {
            service = new EventRegistrationService(eventRepo, guestRepo, registrationRepo, capacityService);
            var item = new EventRegistrationItem(REG_ID, "Событие", Instant.now().plusSeconds(3600), 2, false);
            when(registrationRepo.findByExternalId(REG_ID)).thenReturn(Optional.of(item));
            when(registrationRepo.getEventRowIdByExternalId(REG_ID)).thenReturn(Optional.of(EVENT_ID.internalId()));
            when(eventRepo.getByRowId(EVENT_ID.internalId())).thenReturn(event(false));
            when(capacityService.registeredGuestCount(EVENT_ID.internalId())).thenReturn(9);

            var ex = assertThrows(BusinessConflictException.class, () -> service.updateGuestCount(REG_ID, 4));

            assertEquals(3, ex.details().get("availableSpots"));
            verify(registrationRepo, never()).updateGuestCount(any(), anyInt());
        }

        @Test
        @DisplayName("новое количество меньше 1 → ValidationException")
        void lessThanOne_throwsValidation() {
            service = new EventRegistrationService(eventRepo, guestRepo, registrationRepo, capacityService);

            assertThrows(ValidationException.class, () -> service.updateGuestCount(REG_ID, 0));
            verifyNoInteractions(registrationRepo);
        }

        @Test
        @DisplayName("уже отменена → BusinessConflictException")
        void cancelled_throwsConflict() {
            service = new EventRegistrationService(eventRepo, guestRepo, registrationRepo, capacityService);
            var item = new EventRegistrationItem(REG_ID, "Событие", Instant.now().plusSeconds(3600), 2, true);
            when(registrationRepo.findByExternalId(REG_ID)).thenReturn(Optional.of(item));

            assertThrows(BusinessConflictException.class, () -> service.updateGuestCount(REG_ID, 4));
            verify(registrationRepo, never()).updateGuestCount(any(), anyInt());
        }
    }
}
