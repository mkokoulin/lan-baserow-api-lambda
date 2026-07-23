package com.lan.app.service;

import com.lan.app.domain.model.Event;
import com.lan.app.domain.model.EventCapacityAlert;
import com.lan.app.domain.model.Id;
import com.lan.app.repository.EventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventCapacityAlertService.findDue")
class EventCapacityAlertServiceTest {

    @Mock
    EventRepository eventRepo;
    @Mock
    EventCapacityService capacityService;
    @Mock
    EventCapacityAlertStore alertStore;

    EventCapacityAlertService service;

    static final int ROW_ID = 42;

    static Event event(Integer maxCapacity, boolean soldOut) {
        return new Event(
            new Id(ROW_ID, UUID.randomUUID()),
            "Мастер-класс",
            Instant.now(), Instant.now(),
            "desc", null, null, null, null,
            true, List.of(), null, null,
            true, true, false,
            BigDecimal.ZERO, null,
            maxCapacity, soldOut
        );
    }

    @Nested
    class NoMaxCapacity {

        @Test
        @DisplayName("maxCapacity == null → событие пропускается, состояние не пишется")
        void skipsEvent() {
            service = new EventCapacityAlertService(eventRepo, capacityService, alertStore);
            when(eventRepo.list()).thenReturn(List.of(event(null, false)));

            var result = service.findDue();

            assertTrue(result.isEmpty());
            verifyNoInteractions(alertStore);
            verifyNoInteractions(capacityService);
        }
    }

    @Nested
    class FirstSeen {

        @Test
        @DisplayName("lastKnown == null (первый раз видим событие) → без алерта, но состояние записывается")
        void noAlert_recordsState() {
            service = new EventCapacityAlertService(eventRepo, capacityService, alertStore);
            when(eventRepo.list()).thenReturn(List.of(event(10, false)));
            when(alertStore.getLastKnown(ROW_ID)).thenReturn(null);

            var result = service.findDue();

            assertTrue(result.isEmpty());
            verify(alertStore).recordState(ROW_ID, false);
            verify(alertStore, never()).markAlerted(anyInt());
        }
    }

    @Nested
    class SoldOutToFreeTransition {

        @Test
        @DisplayName("было sold-out, стало свободно, ещё не алертили → алерт создаётся и помечается")
        void transitionsToFree_producesAlert() {
            service = new EventCapacityAlertService(eventRepo, capacityService, alertStore);
            when(eventRepo.list()).thenReturn(List.of(event(10, false)));
            when(alertStore.getLastKnown(ROW_ID)).thenReturn(true);
            when(alertStore.alreadyAlerted(ROW_ID)).thenReturn(false);
            when(capacityService.registeredGuestCount(ROW_ID)).thenReturn(7);

            var result = service.findDue();

            assertEquals(1, result.size());
            EventCapacityAlert alert = result.get(0);
            assertEquals("Мастер-класс", alert.eventName());
            assertEquals(7, alert.registeredCount());
            assertEquals(10, alert.maxCapacity());
            verify(alertStore).markAlerted(ROW_ID);
            verify(alertStore).recordState(ROW_ID, false);
        }

        @Test
        @DisplayName("уже алертили по этому событию → повторный алерт не создаётся")
        void alreadyAlerted_noDuplicateAlert() {
            service = new EventCapacityAlertService(eventRepo, capacityService, alertStore);
            when(eventRepo.list()).thenReturn(List.of(event(10, false)));
            when(alertStore.getLastKnown(ROW_ID)).thenReturn(true);
            when(alertStore.alreadyAlerted(ROW_ID)).thenReturn(true);

            var result = service.findDue();

            assertTrue(result.isEmpty());
            verify(alertStore, never()).markAlerted(anyInt());
            verify(capacityService, never()).registeredGuestCount(anyInt());
            verify(alertStore).recordState(ROW_ID, false);
        }
    }

    @Nested
    class StillSoldOutOrNeverWasSoldOut {

        @Test
        @DisplayName("всё ещё sold-out → без алерта")
        void stillSoldOut_noAlert() {
            service = new EventCapacityAlertService(eventRepo, capacityService, alertStore);
            when(eventRepo.list()).thenReturn(List.of(event(10, true)));
            when(alertStore.getLastKnown(ROW_ID)).thenReturn(true);

            var result = service.findDue();

            assertTrue(result.isEmpty());
            verify(alertStore, never()).markAlerted(anyInt());
            verify(alertStore).recordState(ROW_ID, true);
        }

        @Test
        @DisplayName("никогда не было sold-out → без алерта")
        void neverSoldOut_noAlert() {
            service = new EventCapacityAlertService(eventRepo, capacityService, alertStore);
            when(eventRepo.list()).thenReturn(List.of(event(10, false)));
            when(alertStore.getLastKnown(ROW_ID)).thenReturn(false);

            var result = service.findDue();

            assertTrue(result.isEmpty());
            verify(alertStore, never()).markAlerted(anyInt());
        }
    }

    @Nested
    class MultipleEvents {

        @Test
        @DisplayName("несколько событий → возвращаются только те, что подходят под условие")
        void mixedEvents_returnsOnlyQualifying() {
            service = new EventCapacityAlertService(eventRepo, capacityService, alertStore);
            Event qualifies = event(10, false);
            Event skippedNoCapacity = event(null, false);

            Event stillSoldOut = new Event(
                new Id(99, UUID.randomUUID()), "Другое событие",
                Instant.now(), Instant.now(), "d", null, null, null, null,
                true, List.of(), null, null, true, true, false,
                BigDecimal.ZERO, null, 5, true
            );

            when(eventRepo.list()).thenReturn(List.of(qualifies, skippedNoCapacity, stillSoldOut));
            when(alertStore.getLastKnown(ROW_ID)).thenReturn(true);
            when(alertStore.alreadyAlerted(ROW_ID)).thenReturn(false);
            when(capacityService.registeredGuestCount(ROW_ID)).thenReturn(3);
            when(alertStore.getLastKnown(99)).thenReturn(true);

            var result = service.findDue();

            assertEquals(1, result.size());
            assertEquals("Мастер-класс", result.get(0).eventName());
        }
    }
}
