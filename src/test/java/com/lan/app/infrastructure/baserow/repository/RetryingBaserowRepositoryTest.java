package com.lan.app.infrastructure.baserow.repository;

import com.baserow.exception.BaserowRateLimitedException;
import com.baserow.exception.BaserowUnavailableException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.net.SocketTimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RetryingBaserowRepositoryTest {

    private final RetryingBaserowRepository repo = new RetryingBaserowRepository() {};

    @Test
    void retriesOn429AndReturnsResultOnceItSucceeds() {
        var attempts = new int[]{0};

        var result = repo.execute(() -> {
            attempts[0]++;
            if (attempts[0] < 3) {
                throw webException(429);
            }
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(3, attempts[0]);
    }

    @Test
    void givesUpAfterMaxRateLimitRetries() {
        var attempts = new int[]{0};

        assertThrows(BaserowRateLimitedException.class, () -> repo.execute(() -> {
            attempts[0]++;
            throw webException(429);
        }));

        // initial attempt + 3 retries
        assertEquals(4, attempts[0]);
    }

    @Test
    void doesNotRetryOtherHttpErrors() {
        var attempts = new int[]{0};

        assertThrows(Exception.class, () -> repo.execute(() -> {
            attempts[0]++;
            throw webException(500);
        }));

        assertEquals(1, attempts[0]);
    }

    @Test
    void executeIdempotentRetriesOnceOnTimeoutAndSucceeds() {
        var attempts = new int[]{0};

        var result = repo.executeIdempotent(() -> {
            attempts[0]++;
            if (attempts[0] < 2) {
                throw timeoutException();
            }
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(2, attempts[0]);
    }

    @Test
    void executeIdempotentGivesUpAfterOneTimeoutRetry() {
        var attempts = new int[]{0};

        assertThrows(BaserowUnavailableException.class, () -> repo.executeIdempotent(() -> {
            attempts[0]++;
            throw timeoutException();
        }));

        // initial attempt + 1 retry
        assertEquals(2, attempts[0]);
    }

    @Test
    void plainExecuteDoesNotRetryOnTimeout() {
        var attempts = new int[]{0};

        assertThrows(BaserowUnavailableException.class, () -> repo.execute(() -> {
            attempts[0]++;
            throw timeoutException();
        }));

        assertEquals(1, attempts[0]);
    }

    private static ProcessingException timeoutException() {
        return new ProcessingException(new SocketTimeoutException("timeout"));
    }

    private static WebApplicationException webException(int status) {
        var response = mock(Response.class);
        when(response.getStatus()).thenReturn(status);
        var statusInfo = mock(Response.StatusType.class);
        when(statusInfo.getStatusCode()).thenReturn(status);
        when(statusInfo.getReasonPhrase()).thenReturn(String.valueOf(status));
        when(response.getStatusInfo()).thenReturn(statusInfo);
        return new WebApplicationException(response);
    }
}
