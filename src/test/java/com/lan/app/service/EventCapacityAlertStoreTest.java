package com.lan.app.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EventCapacityAlertStore")
class EventCapacityAlertStoreTest {

    static final int ROW_ID = 7;

    @Test
    @DisplayName("новое событие → getLastKnown возвращает null")
    void unknownEvent_returnsNull() {
        var store = new EventCapacityAlertStore();

        assertNull(store.getLastKnown(ROW_ID));
    }

    @Test
    @DisplayName("recordState сохраняет последнее известное состояние")
    void recordState_storesLatestValue() {
        var store = new EventCapacityAlertStore();

        store.recordState(ROW_ID, true);
        assertEquals(Boolean.TRUE, store.getLastKnown(ROW_ID));

        store.recordState(ROW_ID, false);
        assertEquals(Boolean.FALSE, store.getLastKnown(ROW_ID));
    }

    @Test
    @DisplayName("markAlerted помечает событие как уже заалерченное")
    void markAlerted_setsAlreadyAlerted() {
        var store = new EventCapacityAlertStore();

        assertFalse(store.alreadyAlerted(ROW_ID));
        store.markAlerted(ROW_ID);
        assertTrue(store.alreadyAlerted(ROW_ID));
    }

    @Test
    @DisplayName("recordState(soldOut=true) сбрасывает флаг alerted для события")
    void recordingSoldOut_clearsAlertedFlag() {
        var store = new EventCapacityAlertStore();
        store.markAlerted(ROW_ID);
        assertTrue(store.alreadyAlerted(ROW_ID));

        store.recordState(ROW_ID, true);

        assertFalse(store.alreadyAlerted(ROW_ID));
    }

    @Test
    @DisplayName("recordState(soldOut=false) не сбрасывает флаг alerted")
    void recordingNotSoldOut_keepsAlertedFlag() {
        var store = new EventCapacityAlertStore();
        store.markAlerted(ROW_ID);

        store.recordState(ROW_ID, false);

        assertTrue(store.alreadyAlerted(ROW_ID));
    }

    @Test
    @DisplayName("состояние по разным событиям не пересекается")
    void perEventIsolation() {
        var store = new EventCapacityAlertStore();

        store.recordState(1, true);
        store.markAlerted(1);

        assertNull(store.getLastKnown(2));
        assertFalse(store.alreadyAlerted(2));
    }
}
