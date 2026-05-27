package com.lan.app.infrastructure.baserow.repository;

import com.lan.app.infrastructure.baserow.exception.BaserowException;
import com.lan.app.infrastructure.baserow.exception.BaserowNotFoundException;
import com.lan.app.infrastructure.baserow.exception.BaserowRateLimitedException;
import com.lan.app.infrastructure.baserow.exception.BaserowUnauthorizedException;
import com.lan.app.infrastructure.baserow.exception.BaserowUnavailableException;
import jakarta.ws.rs.WebApplicationException;
import org.jboss.logging.Logger;

import java.net.SocketTimeoutException;
import java.util.UUID;
import java.util.function.Supplier;

public abstract class AbstractBaserowRepository {

    private static final Logger LOG = Logger.getLogger(AbstractBaserowRepository.class);

    protected <T> T execute(Supplier<T> action) {
        try {
            return action.get();
        } catch (BaserowNotFoundException e) {
            throw e;
        } catch (WebApplicationException e) {
            throw translateHttpException(e);
        } catch (Exception e) {
            if (hasTimeoutCause(e)) {
                LOG.error("Baserow request timed out", e);
                throw new BaserowUnavailableException("Baserow is unavailable.", e);
            }
            throw e;
        }
    }

    protected <T> T executeWithEntityNotFound(
        Supplier<T> action,
        String entityName,
        UUID externalId
    ) {
        try {
            return action.get();
        } catch (BaserowNotFoundException e) {
            throw e;
        } catch (WebApplicationException e) {
            if (isNotFound(e)) {
                throw new BaserowNotFoundException(entityName, externalId);
            }
            throw translateHttpException(e);
        } catch (Exception e) {
            if (hasTimeoutCause(e)) {
                LOG.error("Baserow request timed out", e);
                throw new BaserowUnavailableException("Baserow is unavailable.", e);
            }
            throw e;
        }
    }

    private BaserowException translateHttpException(WebApplicationException e) {
        int status = e.getResponse().getStatus();
        if (status == 401) {
            LOG.error("Baserow token is invalid or missing", e);
            return new BaserowUnauthorizedException();
        }
        if (status == 429) {
            LOG.warn("Baserow rate limit exceeded", e);
            return new BaserowRateLimitedException();
        }
        LOG.errorf("Baserow request failed: HTTP %d", status);
        return new BaserowUnavailableException("Baserow request failed.", e);
    }

    protected boolean isNotFound(WebApplicationException e) {
        return e.getResponse() != null && e.getResponse().getStatus() == 404;
    }

    protected boolean hasTimeoutCause(Throwable throwable) {
        while (throwable != null) {
            if (throwable instanceof SocketTimeoutException) {
                return true;
            }
            throwable = throwable.getCause();
        }
        return false;
    }
}
