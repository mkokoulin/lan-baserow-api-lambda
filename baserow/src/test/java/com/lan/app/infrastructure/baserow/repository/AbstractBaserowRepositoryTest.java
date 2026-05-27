package com.lan.app.infrastructure.baserow.repository;

import com.lan.app.infrastructure.baserow.exception.BaserowNotFoundException;
import com.lan.app.infrastructure.baserow.exception.BaserowRateLimitedException;
import com.lan.app.infrastructure.baserow.exception.BaserowUnauthorizedException;
import com.lan.app.infrastructure.baserow.exception.BaserowUnavailableException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.SocketTimeoutException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AbstractBaserowRepositoryTest {

    private AbstractBaserowRepository repo;

    @BeforeEach
    void setup() {
        repo = new AbstractBaserowRepository() {};
    }

    // ── execute() ────────────────────────────────────────────────────────────

    @Test
    void execute_returnsResultOnSuccess() {
        var result = repo.execute(() -> "ok");
        assertEquals("ok", result);
    }

    @Test
    void execute_propagatesBaserowNotFoundException() {
        var id = UUID.randomUUID();
        var ex = new BaserowNotFoundException("Guest", id);

        var thrown = assertThrows(BaserowNotFoundException.class, () -> repo.execute(() -> { throw ex; }));
        assertSame(ex, thrown);
    }

    @Test
    void execute_wraps401ToUnauthorized() {
        assertThrows(BaserowUnauthorizedException.class,
            () -> repo.execute(() -> { throw webException(401); }));
    }

    @Test
    void execute_wraps429ToRateLimited() {
        assertThrows(BaserowRateLimitedException.class,
            () -> repo.execute(() -> { throw webException(429); }));
    }

    @Test
    void execute_wrapsOtherHttpErrorToUnavailable() {
        assertThrows(BaserowUnavailableException.class,
            () -> repo.execute(() -> { throw webException(503); }));
    }

    @Test
    void execute_wraps500ToUnavailable() {
        assertThrows(BaserowUnavailableException.class,
            () -> repo.execute(() -> { throw webException(500); }));
    }

    @Test
    void execute_wrapsTimeoutToUnavailable() {
        var cause = new RuntimeException(new SocketTimeoutException("timed out"));

        assertThrows(BaserowUnavailableException.class,
            () -> repo.execute(() -> { throw cause; }));
    }

    @Test
    void execute_rethrowsNonTimeoutRuntimeException() {
        var cause = new IllegalStateException("unexpected");

        assertThrows(IllegalStateException.class,
            () -> repo.execute(() -> { throw cause; }));
    }

    // ── executeWithEntityNotFound() ───────────────────────────────────────────

    @Test
    void executeWithEntityNotFound_returnsResultOnSuccess() {
        var id = UUID.randomUUID();
        var result = repo.executeWithEntityNotFound(() -> 42, "Tariff", id);
        assertEquals(42, result);
    }

    @Test
    void executeWithEntityNotFound_propagatesBaserowNotFoundException() {
        var id = UUID.randomUUID();
        var ex = new BaserowNotFoundException("Event", id);

        var thrown = assertThrows(BaserowNotFoundException.class,
            () -> repo.executeWithEntityNotFound(() -> { throw ex; }, "Event", id));
        assertSame(ex, thrown);
    }

    @Test
    void executeWithEntityNotFound_maps404ToBaserowNotFoundException() {
        var id = UUID.randomUUID();

        var thrown = assertThrows(BaserowNotFoundException.class,
            () -> repo.executeWithEntityNotFound(() -> { throw webException(404); }, "Booking", id));
        assertEquals("Booking", thrown.entityName());
        assertEquals(id, thrown.externalId());
    }

    @Test
    void executeWithEntityNotFound_wraps401ToUnauthorized() {
        assertThrows(BaserowUnauthorizedException.class,
            () -> repo.executeWithEntityNotFound(() -> { throw webException(401); }, "X", UUID.randomUUID()));
    }

    @Test
    void executeWithEntityNotFound_wraps429ToRateLimited() {
        assertThrows(BaserowRateLimitedException.class,
            () -> repo.executeWithEntityNotFound(() -> { throw webException(429); }, "X", UUID.randomUUID()));
    }

    @Test
    void executeWithEntityNotFound_wrapsOtherHttpErrorToUnavailable() {
        assertThrows(BaserowUnavailableException.class,
            () -> repo.executeWithEntityNotFound(() -> { throw webException(502); }, "X", UUID.randomUUID()));
    }

    @Test
    void executeWithEntityNotFound_wrapsTimeoutToUnavailable() {
        var cause = new RuntimeException(new SocketTimeoutException("read timed out"));

        assertThrows(BaserowUnavailableException.class,
            () -> repo.executeWithEntityNotFound(() -> { throw cause; }, "X", UUID.randomUUID()));
    }

    // ── hasTimeoutCause() ─────────────────────────────────────────────────────

    @Test
    void hasTimeoutCause_returnsTrueForDirectSocketTimeout() {
        assertTrue(repo.hasTimeoutCause(new SocketTimeoutException()));
    }

    @Test
    void hasTimeoutCause_returnsTrueForWrappedSocketTimeout() {
        var wrapped = new RuntimeException(new RuntimeException(new SocketTimeoutException()));
        assertTrue(repo.hasTimeoutCause(wrapped));
    }

    @Test
    void hasTimeoutCause_returnsFalseForUnrelatedExceptions() {
        assertFalse(repo.hasTimeoutCause(new RuntimeException("other")));
    }

    @Test
    void hasTimeoutCause_returnsFalseForNull() {
        assertFalse(repo.hasTimeoutCause(null));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static WebApplicationException webException(int status) {
        return new WebApplicationException(Response.status(status).build());
    }
}
