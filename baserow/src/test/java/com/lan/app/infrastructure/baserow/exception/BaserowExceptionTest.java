package com.lan.app.infrastructure.baserow.exception;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BaserowExceptionTest {

    @Test
    void baserowNotFoundException_storesEntityNameAndExternalId() {
        var id = UUID.randomUUID();
        var ex = new BaserowNotFoundException("Guest", id);

        assertEquals("Guest", ex.entityName());
        assertEquals(id, ex.externalId());
        assertTrue(ex.getMessage().contains("Guest"));
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void baserowNotFoundException_isBaserowException() {
        var ex = new BaserowNotFoundException("Event", UUID.randomUUID());
        assertInstanceOf(BaserowException.class, ex);
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void baserowUnavailableException_storesCause() {
        var cause = new RuntimeException("timeout");
        var ex = new BaserowUnavailableException("Baserow is unavailable.", cause);

        assertEquals("Baserow is unavailable.", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void baserowUnavailableException_isBaserowException() {
        var ex = new BaserowUnavailableException("down", new RuntimeException());
        assertInstanceOf(BaserowException.class, ex);
    }

    @Test
    void baserowDataIntegrityException_messageContainsEntityAndId() {
        var id = UUID.randomUUID();
        var ex = new BaserowDataIntegrityException("CoworkingTariff", id);

        assertTrue(ex.getMessage().contains("CoworkingTariff"));
        assertTrue(ex.getMessage().contains(id.toString()));
    }

    @Test
    void baserowDataIntegrityException_isBaserowException() {
        var ex = new BaserowDataIntegrityException("X", UUID.randomUUID());
        assertInstanceOf(BaserowException.class, ex);
    }

    @Test
    void baserowUnauthorizedException_hasExpectedMessage() {
        var ex = new BaserowUnauthorizedException();

        assertNotNull(ex.getMessage());
        assertFalse(ex.getMessage().isBlank());
        assertInstanceOf(BaserowException.class, ex);
    }

    @Test
    void baserowRateLimitedException_hasExpectedMessage() {
        var ex = new BaserowRateLimitedException();

        assertNotNull(ex.getMessage());
        assertFalse(ex.getMessage().isBlank());
        assertInstanceOf(BaserowException.class, ex);
    }

    @Test
    void allExceptions_extendRuntimeException() {
        assertInstanceOf(RuntimeException.class, new BaserowNotFoundException("X", UUID.randomUUID()));
        assertInstanceOf(RuntimeException.class, new BaserowUnavailableException("x", new RuntimeException()));
        assertInstanceOf(RuntimeException.class, new BaserowDataIntegrityException("X", UUID.randomUUID()));
        assertInstanceOf(RuntimeException.class, new BaserowUnauthorizedException());
        assertInstanceOf(RuntimeException.class, new BaserowRateLimitedException());
    }
}
