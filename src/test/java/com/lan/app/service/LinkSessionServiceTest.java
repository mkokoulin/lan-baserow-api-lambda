package com.lan.app.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("LinkSessionService")
class LinkSessionServiceTest {

    static final UUID GUEST_ID = UUID.randomUUID();

    @Test
    @DisplayName("нет сессии → getStatus возвращает null")
    void noSession_returnsNull() {
        var service = new LinkSessionService();

        assertNull(service.getStatus(GUEST_ID));
    }

    @Test
    @DisplayName("init → PENDING")
    void init_setsPending() {
        var service = new LinkSessionService();

        service.init(GUEST_ID);

        assertEquals(LinkSessionService.LinkStatus.PENDING, service.getStatus(GUEST_ID));
    }

    @Test
    @DisplayName("confirm → CONFIRMED")
    void confirm_setsConfirmed() {
        var service = new LinkSessionService();

        service.init(GUEST_ID);
        service.confirm(GUEST_ID);

        assertEquals(LinkSessionService.LinkStatus.CONFIRMED, service.getStatus(GUEST_ID));
    }

    @Test
    @DisplayName("reject → REJECTED")
    void reject_setsRejected() {
        var service = new LinkSessionService();

        service.init(GUEST_ID);
        service.reject(GUEST_ID);

        assertEquals(LinkSessionService.LinkStatus.REJECTED, service.getStatus(GUEST_ID));
    }

    @Test
    @DisplayName("chatIdConflict → CHAT_ID_CONFLICT")
    void chatIdConflict_setsConflict() {
        var service = new LinkSessionService();

        service.init(GUEST_ID);
        service.chatIdConflict(GUEST_ID);

        assertEquals(LinkSessionService.LinkStatus.CHAT_ID_CONFLICT, service.getStatus(GUEST_ID));
    }

    @Test
    @DisplayName("состояние по разным гостям не пересекается")
    void perGuestIsolation() {
        var service = new LinkSessionService();
        UUID other = UUID.randomUUID();

        service.init(GUEST_ID);
        service.confirm(GUEST_ID);

        assertNull(service.getStatus(other));
    }

    @Test
    @DisplayName("повторный init сбрасывает статус обратно в PENDING")
    void reinit_resetsToPending() {
        var service = new LinkSessionService();

        service.init(GUEST_ID);
        service.confirm(GUEST_ID);
        service.init(GUEST_ID);

        assertEquals(LinkSessionService.LinkStatus.PENDING, service.getStatus(GUEST_ID));
    }
}
