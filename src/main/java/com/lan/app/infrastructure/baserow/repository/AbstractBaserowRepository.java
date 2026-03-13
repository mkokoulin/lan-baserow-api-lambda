package com.lan.app.infrastructure.baserow.repository;

import com.lan.app.infrastructure.baserow.exception.BaserowNotFoundException;
import com.lan.app.infrastructure.baserow.exception.BaserowUnavailableException;
import jakarta.ws.rs.WebApplicationException;

import java.net.SocketTimeoutException;
import java.util.UUID;
import java.util.function.Supplier;

public abstract class AbstractBaserowRepository {

    protected <T> T execute(Supplier<T> action) {
        try {
            return action.get();
        } catch (BaserowNotFoundException e) {
            throw e;
        } catch (WebApplicationException e) {
            throw new BaserowUnavailableException("Baserow request failed.", e);
        } catch (Exception e) {
            if (hasTimeoutCause(e)) {
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
            throw new BaserowUnavailableException("Baserow request failed.", e);
        } catch (Exception e) {
            if (hasTimeoutCause(e)) {
                throw new BaserowUnavailableException("Baserow is unavailable.", e);
            }
            throw e;
        }
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