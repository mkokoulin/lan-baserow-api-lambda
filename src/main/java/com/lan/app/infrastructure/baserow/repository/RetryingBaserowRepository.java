package com.lan.app.infrastructure.baserow.repository;

import com.baserow.exception.BaserowRateLimitedException;
import com.baserow.repository.AbstractBaserowRepository;
import org.jboss.logging.Logger;

import java.net.SocketTimeoutException;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Base class for Baserow repositories that retries a request a few times when Baserow answers
 * with HTTP 429. A 429 means Baserow rejected the request before doing any work, so re-sending it
 * can't double-create a row — unlike a timeout, where we can't tell if the write already landed.
 * That makes rate limiting the one failure mode here that's safe to retry blindly.
 *
 * Bursts of concurrent registrations for a single popular/limited-capacity event are exactly when
 * this gets hit (everyone submitting within the same few seconds): without a retry, the site's
 * guest/registration creation call fails outright, and the caller falls back to a Telegram-only
 * notification with nothing written to Baserow.
 */
public abstract class RetryingBaserowRepository extends AbstractBaserowRepository {

    private static final Logger log = Logger.getLogger(RetryingBaserowRepository.class);

    private static final int MAX_RATE_LIMIT_RETRIES = 3;
    private static final long RATE_LIMIT_BACKOFF_BASE_MS = 200;

    // A timed-out read can be retried blindly too (nothing was written), but unlike a 429 the
    // Baserow call already burned its full read-timeout, so even one retry is expensive against
    // the Lambda's 30s budget. Keep this to a single retry and only use it for idempotent reads
    // (see executeIdempotent) - never for the plain execute()/executeWithEntityNotFound() paths
    // below, which writes also go through.
    private static final int MAX_TIMEOUT_RETRIES = 1;

    @Override
    protected <T> T execute(Supplier<T> action) {
        return withRateLimitRetry(() -> super.execute(action));
    }

    @Override
    protected <T> T executeWithEntityNotFound(Supplier<T> action, String entityName, UUID externalId) {
        return withRateLimitRetry(() -> super.executeWithEntityNotFound(action, entityName, externalId));
    }

    /**
     * Use only for read-only Baserow calls (GET/list). Also retries once on a request timeout,
     * which is safe here because a re-sent read can't double-create or double-mutate anything.
     */
    protected <T> T executeIdempotent(Supplier<T> action) {
        return withRateLimitRetry(() -> withTimeoutRetry(() -> super.execute(action)));
    }

    private <T> T withRateLimitRetry(Supplier<T> call) {
        int attempt = 0;
        while (true) {
            try {
                return call.get();
            } catch (BaserowRateLimitedException e) {
                if (attempt >= MAX_RATE_LIMIT_RETRIES) {
                    throw e;
                }
                attempt++;
                log.warnf("Baserow rate limit exceeded, retrying (attempt %d/%d)", attempt, MAX_RATE_LIMIT_RETRIES);
                sleepBackoff(attempt);
            }
        }
    }

    private void sleepBackoff(int attempt) {
        long delayMs = RATE_LIMIT_BACKOFF_BASE_MS * (1L << (attempt - 1));
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private <T> T withTimeoutRetry(Supplier<T> call) {
        int attempt = 0;
        while (true) {
            try {
                return call.get();
            } catch (RuntimeException e) {
                if (attempt >= MAX_TIMEOUT_RETRIES || !isTimeout(e)) {
                    throw e;
                }
                attempt++;
                log.warnf("Baserow request timed out, retrying (attempt %d/%d)", attempt, MAX_TIMEOUT_RETRIES);
            }
        }
    }

    // Vert.x's io.vertx.core.impl.NoStackTraceTimeoutException (what the Quarkus REST client
    // actually throws on a read timeout) is internal/non-exported, so it's matched by name
    // instead of a compile-time import.
    private boolean isTimeout(Throwable throwable) {
        while (throwable != null) {
            if (throwable instanceof SocketTimeoutException
                || "io.vertx.core.impl.NoStackTraceTimeoutException".equals(throwable.getClass().getName())) {
                return true;
            }
            throwable = throwable.getCause();
        }
        return false;
    }
}
