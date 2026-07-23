package com.lan.app.service;

import com.lan.app.repository.EventRegistrationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventCapacityService")
class EventCapacityServiceTest {

    static final int EVENT_ROW_ID = 5;

    @Mock
    EventRegistrationRepository registrationRepo;

    @Test
    @DisplayName("registeredGuestCount делегирует в repo.countGuests")
    void registeredGuestCount_delegates() {
        var service = new EventCapacityService(registrationRepo);
        when(registrationRepo.countGuests(EVENT_ROW_ID)).thenReturn(4);

        assertEquals(4, service.registeredGuestCount(EVENT_ROW_ID));
    }

    @Test
    @DisplayName("maxCapacity == null → не sold out, repo не вызывается")
    void nullMaxCapacity_neverSoldOut() {
        var service = new EventCapacityService(registrationRepo);

        assertFalse(service.isSoldOut(null, EVENT_ROW_ID));
        verifyNoInteractions(registrationRepo);
    }

    @Test
    @DisplayName("зарегистрировано меньше вместимости → не sold out")
    void underCapacity_notSoldOut() {
        var service = new EventCapacityService(registrationRepo);
        when(registrationRepo.countGuests(EVENT_ROW_ID)).thenReturn(3);

        assertFalse(service.isSoldOut(10, EVENT_ROW_ID));
    }

    @Test
    @DisplayName("зарегистрировано ровно по вместимости → sold out")
    void exactCapacity_soldOut() {
        var service = new EventCapacityService(registrationRepo);
        when(registrationRepo.countGuests(EVENT_ROW_ID)).thenReturn(10);

        assertTrue(service.isSoldOut(10, EVENT_ROW_ID));
    }

    @Test
    @DisplayName("зарегистрировано больше вместимости (овербукинг) → sold out")
    void overCapacity_soldOut() {
        var service = new EventCapacityService(registrationRepo);
        when(registrationRepo.countGuests(EVENT_ROW_ID)).thenReturn(15);

        assertTrue(service.isSoldOut(10, EVENT_ROW_ID));
    }

    @Test
    @DisplayName("maxCapacity == 0 → сразу sold out даже без регистраций")
    void zeroCapacity_soldOut() {
        var service = new EventCapacityService(registrationRepo);
        when(registrationRepo.countGuests(EVENT_ROW_ID)).thenReturn(0);

        assertTrue(service.isSoldOut(0, EVENT_ROW_ID));
    }
}
